/**
 * 权限管理服务实现类：提供动态权限配置功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:20:00
 */
package com.okbug.platform.service.permission.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.permission.request.PermissionCreateRequest;
import com.okbug.platform.dto.permission.request.PermissionQueryRequest;
import com.okbug.platform.dto.permission.request.RolePermissionUpdateRequest;
import com.okbug.platform.dto.permission.response.PermissionTreeResponse;
import com.okbug.platform.dto.permission.response.UserPermissionOverrideResponse;
import com.okbug.platform.dto.permission.request.UserPermissionOverrideUpdateRequest;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.auth.UserPermission;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.auth.UserPermissionMapper;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.entity.auth.RolePermission;
import com.okbug.platform.mapper.auth.PermissionMapper;
import com.okbug.platform.mapper.auth.RolePermissionMapper;
import com.okbug.platform.service.permission.PermissionManagementService;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.service.permission.PermissionService;
import com.okbug.platform.common.cache.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.dao.DuplicateKeyException;

import com.okbug.platform.ws.RealtimeWebSocketHandler;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionManagementServiceImpl implements PermissionManagementService {
    
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionCacheService permissionCacheService;
    private final UserPermissionMapper userPermissionMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final TeamAccessService teamAccessService;
    private final RealtimeWebSocketHandler permissionSocketHandler;
    private final com.okbug.platform.service.system.SystemConfigService systemConfigService;
    private final com.okbug.platform.mapper.auth.UserPermissionContribMapper userPermissionContribMapper;
    
    @Override
    /**
     * 获取全量权限树（启用状态），优先走缓存并返回深拷贝副本。
     *
     * @return 权限树
     */
    public List<PermissionTreeResponse> getPermissionTree() {
        // 优先缓存
        List<PermissionTreeResponse> cached = permissionCacheService.getPermissionTree();
        if (cached != null) {
            // 返回副本，避免外部修改污染缓存基线
            return deepCopyTree(cached);
        }

        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.eq(Permission::getStatus, Permission.STATUS_ENABLED)
             .orderByAsc(Permission::getSort);
        List<Permission> allPermissions = permissionMapper.selectList(query);

        List<PermissionTreeResponse> tree = buildPermissionTree(allPermissions, null);
        permissionCacheService.setPermissionTree(tree);
        return tree;
    }
    
    @Override
    /**
     * 获取指定角色的权限树，并标记已分配权限（selected）。
     *
     * @param role 角色
     * @return 带标记的权限树
     */
    public List<PermissionTreeResponse> getRolePermissionTree(String role) {
        // 优先缓存
        List<PermissionTreeResponse> cached = permissionCacheService.getRolePermissionTree(role);
        if (cached != null) {
            // 返回副本，避免调用方修改污染缓存
            return deepCopyTree(cached);
        }

        // 基于基线树的深拷贝，确保不污染缓存
        List<PermissionTreeResponse> allPermissions = deepCopyTree(getPermissionTree());
        
        // 查询角色已分配的权限ID
        List<Long> rolePermissionIds = getRolePermissionIds(role);
        
        // 标记已分配的权限
        markSelectedPermissions(allPermissions, rolePermissionIds);
        
        permissionCacheService.setRolePermissionTree(role, allPermissions);
        return allPermissions;
    }
    
    @Override
    /**
     * 分页查询权限（支持类型与关键词筛选）。
     */
    public IPage<Permission> getPermissionPage(PermissionQueryRequest request) {
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        
        // 权限类型筛选
        if (StrUtil.isNotBlank(request.getPermissionType())) {
            query.eq(Permission::getPermissionType, request.getPermissionType());
        }
        
        // 关键词搜索
        if (StrUtil.isNotBlank(request.getKeyword())) {
            query.and(wrapper -> wrapper
                .like(Permission::getPermissionCode, request.getKeyword())
                .or()
                .like(Permission::getPermissionName, request.getKeyword())
            );
        }
        
        query.orderByAsc(Permission::getSort);
        
        Page<Permission> page = new Page<>(request.getCurrent(), request.getSize());
        return permissionMapper.selectPage(page, query);
    }
    
    @Override
    /**
     * 根据ID获取权限详情。
     *
     * @param id 权限ID
     * @return 权限实体
     * @throws com.okbug.platform.common.base.ServiceException 不存在时抛出
     */
    public Permission getPermissionById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "权限不存在");
        }
        return permission;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 创建权限。
     *
     * @param request 创建请求
     * @return 权限ID
     */
    public Long createPermission(PermissionCreateRequest request) {
        // 检查权限编码是否已存在
        if (isPermissionCodeExists(request.getPermissionCode(), null)) {
            throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "权限编码已存在");
        }
        
        // 验证父权限
        if (request.getParentId() != null && request.getParentId() > 0) {
            Permission parent = permissionMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "父权限不存在");
            }
        }
        
        // 创建权限
        Permission permission = new Permission();
        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionType(request.getPermissionType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getMenuPath());
        permission.setComponent(request.getComponentPath());
        permission.setIcon(request.getMenuIcon());
        permission.setSort(request.getSort());
        permission.setStatus(Permission.STATUS_ENABLED);
        
        int result = permissionMapper.insert(permission);
        if (result > 0) {
            log.info("权限创建成功，权限ID: {}, 权限编码: {}", permission.getId(), request.getPermissionCode());
            return permission.getId();
        } else {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "权限创建失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新权限（不允许更改为自身为父）。
     */
    public boolean updatePermission(Long id, PermissionCreateRequest request) {
        Permission permission = getPermissionById(id);
        
        // 检查权限编码是否已存在（排除当前权限）
        if (isPermissionCodeExists(request.getPermissionCode(), id)) {
            throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "权限编码已存在");
        }
        
        // 验证父权限
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (Objects.equals(request.getParentId(), id)) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "不能将自己设为父权限");
            }
            Permission parent = permissionMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "父权限不存在");
            }
        }
        
        // 更新权限
        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionType(request.getPermissionType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getMenuPath());
        permission.setComponent(request.getComponentPath());
        permission.setIcon(request.getMenuIcon());
        permission.setSort(request.getSort());
        
        int result = permissionMapper.updateById(permission);
        if (result > 0) {
            log.info("权限更新成功，权限ID: {}, 权限编码: {}", id, request.getPermissionCode());
            return true;
        } else {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "权限更新失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除权限（需无子权限，同时清理角色-权限关联）。
     */
    public boolean deletePermission(Long id) {
        Permission permission = getPermissionById(id);
        
        // 检查是否有子权限
        LambdaQueryWrapper<Permission> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(Permission::getParentId, id);
        Long childCount = permissionMapper.selectCount(childQuery);
        if (childCount > 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "存在子权限，无法删除");
        }
        
        // 删除角色权限关联
        LambdaQueryWrapper<RolePermission> rolePermQuery = new LambdaQueryWrapper<>();
        rolePermQuery.eq(RolePermission::getPermissionId, id);
        rolePermissionMapper.delete(rolePermQuery);
        
        // 删除权限
        int result = permissionMapper.deleteById(id);
        if (result > 0) {
            log.info("权限删除成功，权限ID: {}, 权限编码: {}", id, permission.getPermissionCode());
            return true;
        } else {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "权限删除失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 批量删除权限（逐个校验是否有子权限）。
     */
    public boolean batchDeletePermissions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "权限ID列表不能为空");
        }
        
        // 检查是否有子权限
        for (Long id : ids) {
            LambdaQueryWrapper<Permission> childQuery = new LambdaQueryWrapper<>();
            childQuery.eq(Permission::getParentId, id);
            Long childCount = permissionMapper.selectCount(childQuery);
            if (childCount > 0) {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "权限ID " + id + " 存在子权限，无法删除");
            }
        }
        
        int result = permissionMapper.deleteBatchIds(ids);
        if (result > 0) {
            log.info("批量删除权限成功，权限ID列表: {}, 删除数量: {}", ids, result);
            return true;
        } else {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "批量删除权限失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新权限状态（启用/禁用）。
     */
    public boolean updatePermissionStatus(Long id, Integer status) {
        Permission permission = getPermissionById(id);
        
        if (!Objects.equals(permission.getStatus(), status)) {
            permission.setStatus(status);
            int result = permissionMapper.updateById(permission);
            if (result > 0) {
                log.info("权限状态更新成功，权限ID: {}, 状态: {}", id, status);
                return true;
            } else {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "权限状态更新失败");
            }
        }
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新角色的权限集合：物理清空旧关联并重建；刷新相关缓存与增量贡献；提交后推送WS通知。
     */
    public boolean updateRolePermissions(RolePermissionUpdateRequest request) {
        String role = request.getRole();
        List<Long> permissionIds = request.getPermissionIds();
        
        // 删除原有的角色权限关联（物理删除，避免逻辑删除导致唯一键冲突）
        rolePermissionMapper.deleteByRolePhysical(role);
        
        // 批量插入新的角色权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            
            // 批量插入
            for (RolePermission rolePermission : rolePermissions) {
                rolePermissionMapper.insert(rolePermission);
            }
        }
        
        // 清除角色权限缓存
        permissionCacheService.clearRolePermissions(role);
        permissionCacheService.clearRolePermissionTree(role);
        permissionCacheService.clearPermissionTree();
        
        // 清除使用该角色的所有用户权限缓存
        clearUserPermissionsByRole(role);

        // 当特性开关开启时：重建该角色的用户权限贡献
        Boolean enabled = systemConfigService.getConfigValueAsBoolean(
            com.okbug.platform.common.constants.SystemConfigKeys.PERMISSION_ROLE_CONTRIB_SYNC_ENABLED, false);
        if (Boolean.TRUE.equals(enabled)) {
            rebuildRoleContribution(role);
        }

        log.info("角色权限配置成功，角色: {}, 权限数量: {}", role, permissionIds != null ? permissionIds.size() : 0);

        // 事务提交后异步推送角色变更
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        new Thread(() -> permissionSocketHandler.sendRoleChangedMessage(role)).start();
                    } catch (Exception e) {
                        log.error("事务提交后推送角色权限变更消息失败, role={}", role, e);
                    }
                }
            });
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新用户的权限覆盖配置（仅允许在角色基线上追加额外 ALLOW，不允许削减基线）。
     */
    public boolean updateUserPermissionOverrides(UserPermissionOverrideUpdateRequest request) {
        Long userId = request.getUserId();
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }

        // 操作人-目标用户 严格校验：
        // SUPER_ADMIN 可操作任意用户；ADMIN 仅可操作其 parent_user_id = 自己 的子用户
        Long operatorId = StpUtil.getLoginIdAsLong();
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND, "操作用户不存在或未登录");
        }

        // 业务规则：只增不减 —— 仅允许在角色基线之上新增“额外权限”，不允许去除角色权限
        // 1) 计算角色基线 R
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }

        // 非超级管理员时，仅允许操作自己名下子账号，且禁止操作超级管理员
        if (!operator.isSuperAdmin()) {
            if (user.isSuperAdmin()) {
                throw new ServiceException(ErrorCode.PERMISSION_DENIED, "不可操作超级管理员");
            }
            if (!teamAccessService.canManageUser(operatorId, userId)) {
                throw new ServiceException(ErrorCode.PERMISSION_DENIED, "无权操作该用户");
            }
        }
        List<Long> rolePermissionIds = getRolePermissionIds(user.getRole());

        // 2) 计算前端提交 L
        List<Long> allowList = request.getAllowPermissionIds();
        Set<Long> allow = allowList == null ? new HashSet<>() : new HashSet<>(allowList);

        // 严格模式：如果 L 不是 R 的超集，直接报错，防止去除角色权限
        if (!allow.containsAll(rolePermissionIds)) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能取消角色权限");
        }

        // 3) 仅保留“额外权限”：U = L − R
        Set<Long> extras = new HashSet<>(allow);
        extras.removeAll(rolePermissionIds);

        // 4) 删除旧的“额外权限”记录，保留角色基线对应记录（若曾回填存在）
        userPermissionMapper.deleteExtrasPreservingRole(userId, rolePermissionIds);

        // 5) 查询当前剩余行（通常为保留的角色基线行）以避免重复插入
        LambdaQueryWrapper<UserPermission> remainQuery = new LambdaQueryWrapper<>();
        remainQuery.eq(UserPermission::getUserId, userId)
                   .eq(UserPermission::getIsDeleted, 0);
        List<UserPermission> remaining = userPermissionMapper.selectList(remainQuery);
        Set<Long> existingPermissionIds = remaining.stream()
                .map(UserPermission::getPermissionId)
                .collect(Collectors.toSet());

        // 6) 确保角色基线行存在（在未开启角色贡献合并的情况下，生效权限仅来源于 user_permissions 表）
        if (rolePermissionIds != null && !rolePermissionIds.isEmpty()) {
            for (Long pid : rolePermissionIds) {
                if (pid == null) continue;
                if (existingPermissionIds.contains(pid)) continue;
                UserPermission base = new UserPermission();
                base.setUserId(userId);
                base.setPermissionId(pid);
                base.setGrantType(UserPermission.GRANT_ALLOW);
                try {
                    userPermissionMapper.insert(base);
                } catch (DuplicateKeyException ignore) {
                }
            }
        }

        // 7) 写入新的“额外权限”
        if (!extras.isEmpty()) {
            for (Long pid : extras) {
                if (pid == null) continue;
                if (existingPermissionIds.contains(pid)) continue;
                UserPermission up = new UserPermission();
                up.setUserId(userId);
                up.setPermissionId(pid);
                up.setGrantType(UserPermission.GRANT_ALLOW);
                userPermissionMapper.insert(up);
            }
        }

        // 清除该用户权限缓存
        permissionCacheService.clearUserPermissions(userId);

        log.info("更新用户权限成功，用户ID: {}, 权限数: {}", userId,
                request.getAllowPermissionIds() != null ? request.getAllowPermissionIds().size() : 0);

        // 事务提交后异步推送用户变更
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        new Thread(() -> permissionSocketHandler.sendUserChangedMessage(userId)).start();
                    } catch (Exception e) {
                        log.error("事务提交后推送用户权限变更消息失败, userId={}", userId, e);
                    }
                }
            });
        }
        return true;
    }

    @Override
    /**
     * 读取用户的权限覆盖配置（含基线、锁定父节点）。
     */
    public UserPermissionOverrideResponse getUserPermissionOverrides(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }

        // 读取覆盖配置也进行严格校验，避免越权探测
        Long operatorId = StpUtil.getLoginIdAsLong();
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND, "操作用户不存在或未登录");
        }
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!operator.isSuperAdmin()) {
            if (target.isSuperAdmin()) {
                throw new ServiceException(ErrorCode.PERMISSION_DENIED, "不可查看超级管理员权限");
            }
            if (!teamAccessService.canManageUser(operatorId, userId)) {
                throw new ServiceException(ErrorCode.PERMISSION_DENIED, "无权查看该用户权限");
            }
        }

        // 计算角色基线权限ID集合
        Set<Long> baselineIds = new HashSet<>();
        try {
            Boolean contribSyncEnabled = systemConfigService.getConfigValueAsBoolean(
                com.okbug.platform.common.constants.SystemConfigKeys.PERMISSION_ROLE_CONTRIB_SYNC_ENABLED, false);

            if (Boolean.TRUE.equals(contribSyncEnabled)) {
                // 基于贡献表识别基线（ROLE）
                LambdaQueryWrapper<com.okbug.platform.entity.auth.UserPermissionContrib> upcQuery = new LambdaQueryWrapper<>();
                upcQuery.eq(com.okbug.platform.entity.auth.UserPermissionContrib::getUserId, userId)
                        .eq(com.okbug.platform.entity.auth.UserPermissionContrib::getIsDeleted, 0)
                        .eq(com.okbug.platform.entity.auth.UserPermissionContrib::getSourceType,
                                com.okbug.platform.entity.auth.UserPermissionContrib.SOURCE_TYPE_ROLE);
                List<com.okbug.platform.entity.auth.UserPermissionContrib> contribRows = userPermissionContribMapper.selectList(upcQuery);
                if (contribRows != null) {
                    for (com.okbug.platform.entity.auth.UserPermissionContrib row : contribRows) {
                        if (row != null && row.getPermissionId() != null) {
                            baselineIds.add(row.getPermissionId());
                        }
                    }
                }
            } else {
                // 基于角色关联表识别基线
                List<Long> rolePermissionIds = getRolePermissionIds(target.getRole());
                if (rolePermissionIds != null) {
                    baselineIds.addAll(rolePermissionIds);
                }
            }
        } catch (Exception e) {
            log.warn("计算用户{}的基线权限失败，降级为空基线: {}", userId, e.getMessage());
        }

        // 查询用户的手工覆盖（ALLOW/DENY）
        LambdaQueryWrapper<UserPermission> query = new LambdaQueryWrapper<>();
        query.eq(UserPermission::getUserId, userId)
             .eq(UserPermission::getIsDeleted, 0);
        List<UserPermission> list = userPermissionMapper.selectList(query);

        Set<Long> allow = new HashSet<>();
        Set<Long> deny = new HashSet<>();
        for (UserPermission up : list) {
            if (UserPermission.GRANT_ALLOW.equals(up.getGrantType())) {
                allow.add(up.getPermissionId());
            } else if (UserPermission.GRANT_DENY.equals(up.getGrantType())) {
                deny.add(up.getPermissionId());
            }
        }

        // overrides 仅返回“额外授予”的差集：allow − baseline
        if (!baselineIds.isEmpty() && !allow.isEmpty()) {
            allow.removeAll(baselineIds);
        }

        // 计算需要锁定的父级：当某父节点的任何子节点在基线内时，父节点应禁用
        Set<Long> lockedParentIds = new HashSet<>();
        if (!baselineIds.isEmpty()) {
            LambdaQueryWrapper<Permission> pQuery = new LambdaQueryWrapper<>();
            pQuery.in(Permission::getId, baselineIds)
                  .eq(Permission::getIsDeleted, 0);
            List<Permission> baselinePerms = permissionMapper.selectList(pQuery);
            for (Permission p : baselinePerms) {
                if (p.getParentId() != null && p.getParentId() > 0) {
                    lockedParentIds.add(p.getParentId());
                }
            }
        }

        return UserPermissionOverrideResponse.of(
                userId,
                new ArrayList<>(allow),
                new ArrayList<>(deny),
                new ArrayList<>(baselineIds),
                new ArrayList<>(lockedParentIds)
        );
    }

    @Override
    /**
     * 获取用户的生效权限对象列表。
     */
    public List<Permission> getUserEffectivePermissions(Long userId) {
        List<String> codes = getUserEffectivePermissionCodes(userId);
        if (codes.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.in(Permission::getPermissionCode, codes)
             .eq(Permission::getStatus, Permission.STATUS_ENABLED)
             .orderByAsc(Permission::getSort);
        return permissionMapper.selectList(query);
    }

    @Override
    /**
     * 获取用户的生效权限编码列表。
     */
    public List<String> getUserEffectivePermissionCodes(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }
        // 直接复用权限服务的生效权限计算（基于用户-权限 ALLOW；超级管理员特殊处理）
        return permissionService.getEffectivePermissionCodes(userId);
    }

    @Override
    public List<PermissionTreeResponse> getUserPermissionTree(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }

        // 获取完整权限树（深拷贝）
        List<PermissionTreeResponse> tree = deepCopyTree(getPermissionTree());
        if (tree == null) {
            return new ArrayList<>();
        }

        // 获取用户生效权限（ID集合）
        List<Permission> effectivePerms = getUserEffectivePermissions(userId);
        Set<Long> selectedIds = effectivePerms.stream()
                .filter(Objects::nonNull)
                .map(Permission::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 计算需要禁用的父节点：任何子节点在角色基线内，则父节点禁用
        UserPermissionOverrideResponse overrides = getUserPermissionOverrides(userId);
        Set<Long> lockedParentIds = new HashSet<>(overrides.getLockedParentPermissionIds() == null ? Collections.emptyList() : overrides.getLockedParentPermissionIds());

        // 标记 tree
        applySelectionAndDisabled(tree, selectedIds, lockedParentIds);
        return tree;
    }
    
    @Override
    /**
     * 获取指定角色的权限对象列表。
     */
    public List<Permission> getRolePermissions(String role) {
        List<Long> permissionIds = getRolePermissionIds(role);
        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.in(Permission::getId, permissionIds)
             .eq(Permission::getStatus, Permission.STATUS_ENABLED)
             .orderByAsc(Permission::getSort);
        
        return permissionMapper.selectList(query);
    }
    
    @Override
    /**
     * 校验权限编码是否已存在（可排除某ID）。
     */
    public boolean isPermissionCodeExists(String permissionCode, Long excludeId) {
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.eq(Permission::getPermissionCode, permissionCode);
        if (excludeId != null) {
            query.ne(Permission::getId, excludeId);
        }
        
        return permissionMapper.selectCount(query) > 0;
    }
    
    @Override
    /**
     * 刷新所有权限相关缓存。
     */
    public void refreshAllPermissionCache() {
        // 刷新所有权限缓存
        permissionCacheService.refreshAllPermissionCache();
        log.info("权限缓存刷新完成");
    }
    
    // ================ 私有方法 ================
    
    /**
     * 获取角色的权限ID列表
     */
    private List<Long> getRolePermissionIds(String role) {
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRole, role);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(query);
        
        return rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建权限树形结构
     */
    private List<PermissionTreeResponse> buildPermissionTree(List<Permission> permissions, Long parentId) {
        List<PermissionTreeResponse> tree = new ArrayList<>();
        
        for (Permission permission : permissions) {
            if (Objects.equals(permission.getParentId(), parentId)) {
                PermissionTreeResponse node = PermissionTreeResponse.fromPermission(permission);
                
                // 递归构建子节点
                List<PermissionTreeResponse> children = buildPermissionTree(permissions, permission.getId());
                node.setChildren(children);
                
                tree.add(node);
            }
        }
        
        // 按排序号排序
        tree.sort(Comparator.comparing(PermissionTreeResponse::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        
        return tree;
    }
    
    /**
     * 标记已选中的权限
     */
    private void markSelectedPermissions(List<PermissionTreeResponse> permissions, List<Long> selectedIds) {
        if (permissions == null || selectedIds == null) return;
        Set<Long> idSet = new HashSet<>(selectedIds);
        for (PermissionTreeResponse node : permissions) {
            if (node == null) continue;
            node.setSelected(idSet.contains(node.getId()));
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markSelectedPermissions(node.getChildren(), selectedIds);
            }
        }
    }

    private void applySelectionAndDisabled(List<PermissionTreeResponse> nodes, Set<Long> selectedIds, Set<Long> disabledParentIds) {
        if (nodes == null) return;
        for (PermissionTreeResponse node : nodes) {
            if (node == null) continue;
            node.setSelected(selectedIds != null && selectedIds.contains(node.getId()));
            node.setDisabled(disabledParentIds != null && disabledParentIds.contains(node.getId()));
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                applySelectionAndDisabled(node.getChildren(), selectedIds, disabledParentIds);
            }
        }
    }

    /**
     * 深拷贝权限树，避免对缓存基线产生副作用
     */
    private List<PermissionTreeResponse> deepCopyTree(List<PermissionTreeResponse> source) {
        if (source == null) return null;
        List<PermissionTreeResponse> copy = new ArrayList<>(source.size());
        for (PermissionTreeResponse n : source) {
            copy.add(copyNode(n));
        }
        return copy;
    }

    private PermissionTreeResponse copyNode(PermissionTreeResponse node) {
        if (node == null) return null;
        PermissionTreeResponse c = new PermissionTreeResponse();
        c.setId(node.getId());
        c.setPermissionCode(node.getPermissionCode());
        c.setPermissionName(node.getPermissionName());
        c.setPermissionType(node.getPermissionType());
        c.setParentId(node.getParentId());
        c.setMenuPath(node.getMenuPath());
        c.setComponentPath(node.getComponentPath());
        c.setMenuIcon(node.getMenuIcon());
        c.setSort(node.getSort());
        c.setStatus(node.getStatus());
        c.setDescription(node.getDescription());
        c.setSelected(node.getSelected());
        c.setDisabled(node.getDisabled());
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            List<PermissionTreeResponse> childCopies = new ArrayList<>(node.getChildren().size());
            for (PermissionTreeResponse child : node.getChildren()) {
                childCopies.add(copyNode(child));
            }
            c.setChildren(childCopies);
        } else {
            c.setChildren(new ArrayList<>());
        }
        return c;
    }
    
    /**
     * 清除指定角色的所有用户权限缓存
     * 
     * @param role 角色名称
     */
    private void clearUserPermissionsByRole(String role) {
        try {
            // 查询使用该角色的所有用户ID
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getRole, role)
                    .eq(User::getIsDeleted, 0)
                    .select(User::getId);
            List<User> users = userMapper.selectList(userQuery);
            
            if (users.isEmpty()) {
                log.debug("角色 {} 下无用户，跳过用户权限缓存清除", role);
                return;
            }
            
            // 批量清除用户权限缓存
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            permissionCacheService.clearUserPermissions(userIds);
            
            log.info("已清除角色 {} 下 {} 个用户的权限缓存", role, userIds.size());
        } catch (Exception e) {
            log.error("清除角色 {} 用户权限缓存失败", role, e);
        }
    }

    /**
     * 重建该角色对用户的权限贡献（只操作 user_permission_contrib 表，不影响用户手工覆盖）
     */
    private void rebuildRoleContribution(String role) {
        try {
            // 查询该角色的所有用户
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getRole, role)
                    .eq(User::getIsDeleted, 0)
                    .select(User::getId);
            List<User> users = userMapper.selectList(userQuery);
            if (users.isEmpty()) {
                log.info("角色 {} 无用户，跳过贡献重建", role);
                return;
            }

            // 查询该角色的权限ID
            List<Long> permissionIds = getRolePermissionIds(role);

            // 删除旧贡献（逐用户，避免大SQL阻塞；数据量大时可批处理优化）
            for (User u : users) {
                userPermissionContribMapper.deleteRoleContributionForUser(role, u.getId());
            }

            // 回填贡献
            if (!permissionIds.isEmpty()) {
                for (User u : users) {
                    for (Long pid : permissionIds) {
                        com.okbug.platform.entity.auth.UserPermissionContrib row = new com.okbug.platform.entity.auth.UserPermissionContrib();
                        row.setUserId(u.getId());
                        row.setPermissionId(pid);
                        row.setSourceType(com.okbug.platform.entity.auth.UserPermissionContrib.SOURCE_TYPE_ROLE);
                        row.setSourceId(role);
                        try {
                            userPermissionContribMapper.insert(row);
                        } catch (Exception e) {
                            // 并发或唯一冲突时容忍
                        }
                    }
                }
            }

            log.info("已重建角色 {} 对 {} 个用户的权限贡献，权限数 {}", role, users.size(), permissionIds.size());
        } catch (Exception e) {
            log.error("重建角色贡献失败, role={} ", role, e);
            throw new ServiceException(ErrorCode.TASK_EXECUTION_FAILED, "角色权限贡献重建失败");
        }
    }
} 