/**
 * 权限管理服务实现类：实现权限相关的业务逻辑
 * 
 * 核心功能：
 * 1. 用户权限查询和验证
 * 2. 菜单权限构建
 * 3. 数据权限控制
 * 4. 权限缓存管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:10:00
 */
package com.okbug.platform.service.permission.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.auth.response.MenuResponse;
import com.okbug.platform.dto.auth.response.UserPermissionResponse;
import com.okbug.platform.dto.auth.response.LoginPermissionInfo;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.entity.auth.RolePermission;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.auth.PermissionMapper;
import com.okbug.platform.mapper.auth.RolePermissionMapper;
import com.okbug.platform.mapper.auth.UserPermissionMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.common.cache.PermissionCacheService;
import com.okbug.platform.service.permission.PermissionService;
import org.springframework.dao.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final PermissionCacheService permissionCacheService;
    private final com.okbug.platform.service.system.SystemConfigService systemConfigService;
    private final com.okbug.platform.mapper.auth.UserPermissionContribMapper userPermissionContribMapper;
    
    @Override
    /**
     * 获取当前登录用户的权限信息（角色 + 扁平权限清单）。
     *
     * @return 用户权限响应对象
     * @throws com.okbug.platform.common.base.ServiceException 当用户不存在时抛出
     */
    public UserPermissionResponse getCurrentUserPermissions() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 获取用户生效权限列表（仅基于用户-权限关联 ALLOW）
        List<String> permissionCodes = getEffectivePermissionCodes(currentUserId);
        
        // 获取权限详细信息
        List<UserPermissionResponse.PermissionInfo> permissions = new ArrayList<>();
        if (!permissionCodes.isEmpty()) {
            LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
            query.in(Permission::getPermissionCode, permissionCodes)
                 .eq(Permission::getStatus, Permission.STATUS_ENABLED);
            
            List<Permission> permissionList = permissionMapper.selectList(query);
            permissions = permissionList.stream()
                    .map(p -> UserPermissionResponse.PermissionInfo.create(
                            p.getPermissionCode(), p.getPermissionName(), p.getPermissionType()))
                    .collect(Collectors.toList());
        }
        
        log.info("获取用户权限成功，用户ID: {}, 权限数量: {}", currentUserId, permissions.size());
        return UserPermissionResponse.create(user.getRole(), permissions);
    }
    
    @Override
    /**
     * 获取当前登录用户的菜单树。
     *
     * 说明：基于生效权限过滤出菜单类型权限，并按 sort 构建树结构返回。
     *
     * @return 菜单响应列表
     * @throws com.okbug.platform.common.base.ServiceException 当用户不存在时抛出
     */
    public List<MenuResponse> getCurrentUserMenus() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 获取用户生效权限对应的权限列表
        List<String> effectiveCodes = getEffectivePermissionCodes(currentUserId);
        if (effectiveCodes.isEmpty()) {
            log.info("用户无生效权限，用户ID: {}, 角色: {}", currentUserId, user.getRole());
            return new ArrayList<>();
        }
        
        // 查询菜单权限
        List<Permission> menuPermissions = new ArrayList<>();
        if (!effectiveCodes.isEmpty()) {
            LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
            query.in(Permission::getPermissionCode, effectiveCodes)
                 .eq(Permission::getPermissionType, Permission.TYPE_MENU)
                 .eq(Permission::getStatus, Permission.STATUS_ENABLED)
                 .orderByAsc(Permission::getSort);
            
            menuPermissions = permissionMapper.selectList(query);
        }
        
        // 构建菜单树
        List<MenuResponse> menuTree = buildMenuTree(menuPermissions);
        
        log.info("获取用户菜单成功，用户ID: {}, 菜单数量: {}", currentUserId, menuTree.size());
        return menuTree;
    }
    
    @Override
    /**
     * 判断当前登录用户是否拥有指定权限编码。
     *
     * @param permissionCode 权限编码
     * @return 拥有返回 true，否则 false
     */
    public boolean hasPermission(String permissionCode) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return false;
        }
        
        // 超级管理员拥有所有权限
        if (user.isSuperAdmin()) {
            return true;
        }
        
        // 获取用户生效权限列表
        List<String> userPermissions = getEffectivePermissionCodes(currentUserId);
        boolean has = userPermissions.contains(permissionCode);
        log.debug("hasPermission: userId={}, code={}, result={}", currentUserId, permissionCode, has);
        return has;
    }
    
    @Override
    /**
     * 判断当前登录用户是否为指定角色。
     *
     * @param role 角色（USER/ADMIN/SUPER_ADMIN）
     * @return 是则 true
     */
    public boolean hasRole(String role) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return false;
        }
        
        boolean match = role.equals(user.getRole());
        log.debug("hasRole: userId={}, role={}, match={}", currentUserId, role, match);
        return match;
    }
    
    @Override
    /**
     * 校验当前登录用户对目标用户数据的访问权限。
     * 规则：
     * - USER: 仅自己
     * - ADMIN: 自己 + 子账号
     * - SUPER_ADMIN: 全部
     *
     * @param targetUserId 目标用户ID
     * @return 是否有访问权限
     */
    public boolean hasDataPermission(Long targetUserId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            return false;
        }
        
        // 数据权限验证逻辑
        switch (currentUser.getRole()) {
            case User.ROLE_USER:
                // 普通用户只能访问自己的数据
                return Objects.equals(currentUserId, targetUserId);
                
            case User.ROLE_ADMIN:
                // 管理员可以访问自己和子账号的数据
                if (Objects.equals(currentUserId, targetUserId)) {
                    return true;
                }
                // 检查是否为子账号
                User targetUser = userMapper.selectById(targetUserId);
                return targetUser != null && Objects.equals(currentUserId, targetUser.getParentUserId());
                
            case User.ROLE_SUPER_ADMIN:
                // 超级管理员可以访问所有数据
                return true;
                
            default:
                return false;
        }
    }
    
    @Override
    /**
     * 按角色获取权限编码列表（用于后台查看/配置，不影响生效计算）。
     *
     * @param role 角色
     * @return 权限编码列表
     */
    public List<String> getPermissionsByRole(String role) {
        // 先尝试从缓存获取（仅用于后台查看，不影响生效计算）
        List<String> cachedPermissions = permissionCacheService.getRolePermissions(role);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }
        
        // 查询角色权限关联
        LambdaQueryWrapper<RolePermission> rolePermQuery = new LambdaQueryWrapper<>();
        rolePermQuery.eq(RolePermission::getRole, role);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermQuery);
        
        if (rolePermissions.isEmpty()) {
            List<String> emptyList = new ArrayList<>();
            permissionCacheService.setRolePermissions(role, emptyList);
            return emptyList;
        }
        
        // 获取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        
        // 查询权限详细信息
        LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
        permQuery.in(Permission::getId, permissionIds)
                 .eq(Permission::getStatus, Permission.STATUS_ENABLED);
        List<Permission> permissions = permissionMapper.selectList(permQuery);
        
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
        
        // 设置缓存
        permissionCacheService.setRolePermissions(role, permissionCodes);
        
        return permissionCodes;
    }
    
    @Override
    /**
     * 刷新权限缓存。
     *
     * @param userId 指定用户ID；为 null 表示刷新全部缓存
     */
    public void refreshPermissionCache(Long userId) {
        // 清除用户权限缓存
        if (userId != null) {
            permissionCacheService.clearUserPermissions(userId);
            log.info("权限缓存刷新完成，用户ID: {}", userId);
        } else {
            // 刷新所有权限缓存
            permissionCacheService.refreshAllPermissionCache();
            log.info("所有权限缓存刷新完成");
        }
    }
    
    @Override
    /**
     * 获取登录用户的完整权限信息（登录专用）。
     *
     * @param userId 用户ID
     * @return 登录权限信息列表
     */
    public List<LoginPermissionInfo> getLoginUserPermissions(Long userId) {
        log.info("获取登录用户权限信息，用户ID: {}", userId);
        
        // 获取用户角色
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            return new ArrayList<>();
        }
        
        // 获取用户生效权限编码列表
        List<String> effectiveCodes = getEffectivePermissionCodes(userId);
        if (effectiveCodes.isEmpty()) {
            log.info("用户无生效权限，用户ID: {}, 角色: {}", userId, user.getRole());
            return new ArrayList<>();
        }
        
        // 查询完整权限信息
        LambdaQueryWrapper<Permission> query = new LambdaQueryWrapper<>();
        query.in(Permission::getPermissionCode, effectiveCodes)
             .eq(Permission::getStatus, Permission.STATUS_ENABLED)
             .orderByAsc(Permission::getSort);
        List<Permission> permissions = permissionMapper.selectList(query);
        
        // 转换为登录权限信息
        List<LoginPermissionInfo> loginPermissions = permissions.stream()
                .map(this::convertToLoginPermissionInfo)
                .collect(Collectors.toList());
        
        log.info("获取登录用户权限信息成功，用户ID: {}, 权限数量: {}", userId, loginPermissions.size());
        return loginPermissions;
    }

    @Override
    /**
     * 计算用户生效的权限编码列表。
     * 规则：仅读取 user_permissions 中 ALLOW 的权限；
     * 特例：超级管理员在未显式分配时授予全部启用权限；
     * 当开启角色贡献合并开关时，合并角色贡献表。
     *
     * @param userId 用户ID
     * @return 生效权限编码列表
     */
    public List<String> getEffectivePermissionCodes(Long userId) {
        // 优先从缓存读取（软命中：空列表视为未命中，触发一次性回源）
        List<String> cached = permissionCacheService.getUserPermissions(userId);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }

        // 默认：仅基于用户-权限关联（ALLOW）
        LambdaQueryWrapper<com.okbug.platform.entity.auth.UserPermission> upQuery = new LambdaQueryWrapper<>();
        upQuery.eq(com.okbug.platform.entity.auth.UserPermission::getUserId, userId)
               .eq(com.okbug.platform.entity.auth.UserPermission::getIsDeleted, 0)
               .eq(com.okbug.platform.entity.auth.UserPermission::getGrantType, com.okbug.platform.entity.auth.UserPermission.GRANT_ALLOW);
        List<com.okbug.platform.entity.auth.UserPermission> rows = userPermissionMapper.selectList(upQuery);

        List<String> result;
        Boolean contribSyncEnabled = systemConfigService.getConfigValueAsBoolean(
            com.okbug.platform.common.constants.SystemConfigKeys.PERMISSION_ROLE_CONTRIB_SYNC_ENABLED, false);
        if (rows.isEmpty() && !Boolean.TRUE.equals(contribSyncEnabled)) {
            // 若为超级管理员且未显式分配，则授予所有启用权限
            if (user.isSuperAdmin()) {
                LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
                permQuery.eq(Permission::getStatus, Permission.STATUS_ENABLED);
                List<Permission> allPerms = permissionMapper.selectList(permQuery);
                result = allPerms.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
            } else {
                // 非超级管理员：为兼容历史数据，首次访问时按角色回填 ALLOW
                List<Long> rolePermissionIds = getRolePermissionIds(user.getRole());
                if (!rolePermissionIds.isEmpty()) {
                    for (Long permissionId : rolePermissionIds) {
                        com.okbug.platform.entity.auth.UserPermission up = new com.okbug.platform.entity.auth.UserPermission();
                        up.setUserId(userId);
                        up.setPermissionId(permissionId);
                        up.setGrantType(com.okbug.platform.entity.auth.UserPermission.GRANT_ALLOW);
                        try {
                            userPermissionMapper.insert(up);
                        } catch (DuplicateKeyException ex) {
                            // 并发回填下的幂等保障：忽略唯一键冲突
                            log.debug("用户权限回填并发写入已存在，userId: {}, permissionId: {}", userId, permissionId);
                        }
                    }
                    // 重新查询
                    LambdaQueryWrapper<com.okbug.platform.entity.auth.UserPermission> reQuery = new LambdaQueryWrapper<>();
                    reQuery.eq(com.okbug.platform.entity.auth.UserPermission::getUserId, userId)
                          .eq(com.okbug.platform.entity.auth.UserPermission::getIsDeleted, 0)
                          .eq(com.okbug.platform.entity.auth.UserPermission::getGrantType, com.okbug.platform.entity.auth.UserPermission.GRANT_ALLOW);
                    rows = userPermissionMapper.selectList(reQuery);
                }
                if (rows.isEmpty()) {
                    result = new ArrayList<>();
                } else {
                    List<Long> permissionIds = rows.stream()
                            .map(com.okbug.platform.entity.auth.UserPermission::getPermissionId)
                            .distinct()
                            .collect(Collectors.toList());
                    if (permissionIds.isEmpty()) {
                        result = new ArrayList<>();
                    } else {
                        LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
                        permQuery.in(Permission::getId, permissionIds)
                                 .eq(Permission::getStatus, Permission.STATUS_ENABLED);
                        List<Permission> permissions = permissionMapper.selectList(permQuery);
                        result = permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
                    }
                }
            }
        } else {
            // 如果开关开启：合并角色贡献表
            if (Boolean.TRUE.equals(contribSyncEnabled)) {
                // 查询角色贡献
                LambdaQueryWrapper<com.okbug.platform.entity.auth.UserPermissionContrib> contribQuery = new LambdaQueryWrapper<>();
                contribQuery.eq(com.okbug.platform.entity.auth.UserPermissionContrib::getUserId, userId)
                            .eq(com.okbug.platform.entity.auth.UserPermissionContrib::getIsDeleted, 0)
                            .eq(com.okbug.platform.entity.auth.UserPermissionContrib::getSourceType, com.okbug.platform.entity.auth.UserPermissionContrib.SOURCE_TYPE_ROLE);
                List<com.okbug.platform.entity.auth.UserPermissionContrib> contribRows = userPermissionContribMapper.selectList(contribQuery);

                Set<Long> combinedIds = new HashSet<>();
                if (rows != null) {
                    combinedIds.addAll(rows.stream().map(com.okbug.platform.entity.auth.UserPermission::getPermissionId).collect(Collectors.toSet()));
                }
                if (contribRows != null) {
                    combinedIds.addAll(contribRows.stream().map(com.okbug.platform.entity.auth.UserPermissionContrib::getPermissionId).collect(Collectors.toSet()));
                }

                if (combinedIds.isEmpty()) {
                    // 回退到原逻辑
                } else {
                    LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
                    permQuery.in(Permission::getId, combinedIds)
                             .eq(Permission::getStatus, Permission.STATUS_ENABLED);
                    List<Permission> permissions = permissionMapper.selectList(permQuery);
                    result = permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
                    permissionCacheService.setUserPermissions(userId, result);
                    return result;
                }
            }
            List<Long> permissionIds = rows.stream()
                    .map(com.okbug.platform.entity.auth.UserPermission::getPermissionId)
                    .distinct()
                    .collect(Collectors.toList());

            if (permissionIds.isEmpty()) {
                result = new ArrayList<>();
            } else {
                LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
                permQuery.in(Permission::getId, permissionIds)
                         .eq(Permission::getStatus, Permission.STATUS_ENABLED);
                List<Permission> permissions = permissionMapper.selectList(permQuery);
                result = permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
            }
        }

        // 缓存写入：由缓存层对空列表采用短TTL，避免长时间缓存空结果
        permissionCacheService.setUserPermissions(userId, result);
        return result;
    }
    // 删除旧的角色并集/覆盖计算方法（改为仅基于用户-权限关联）
    
    // ================ 私有方法 ================
    
    /**
     * 获取角色对应的权限ID列表
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
     * 计算用户生效的权限编码列表：角色权限 ∪ 用户显式允许 − 用户显式拒绝
     */
    // （移除）
    
    /**
     * 构建菜单树
     */
    private List<MenuResponse> buildMenuTree(List<Permission> permissions) {
        if (permissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 转换为MenuResponse
        List<MenuResponse> allMenus = permissions.stream()
                .map(this::convertToMenuResponse)
                .collect(Collectors.toList());
        
        // 构建树形结构
        Map<Long, List<MenuResponse>> childrenMap = allMenus.stream()
                .filter(menu -> menu.getId() != null)
                .collect(Collectors.groupingBy(menu -> {
                    Permission permission = permissions.stream()
                            .filter(p -> p.getId().equals(menu.getId()))
                            .findFirst()
                            .orElse(null);
                    return permission != null && permission.getParentId() != null ?
                            permission.getParentId() : 0L;
                }));
        
        // 设置子菜单
        allMenus.forEach(menu -> {
            List<MenuResponse> children = childrenMap.get(menu.getId());
            if (children != null) {
                menu.setChildren(children);
            }
        });
        
        // 返回顶级菜单
        return childrenMap.getOrDefault(0L, new ArrayList<>());
    }
    
    /**
     * 转换Permission为MenuResponse
     */
    private MenuResponse convertToMenuResponse(Permission permission) {
        MenuResponse menu = new MenuResponse();
        menu.setId(permission.getId());
        menu.setName(permission.getPermissionName());
        menu.setPath(permission.getPath());
        menu.setComponent(permission.getComponent());
        menu.setIcon(permission.getIcon());
        menu.setChildren(new ArrayList<>());
        return menu;
    }
    
    // 已移除未使用的根据ID批量查询方法，避免冗余代码
    
    /**
     * 转换Permission为LoginPermissionInfo
     */
    private LoginPermissionInfo convertToLoginPermissionInfo(Permission permission) {
        return LoginPermissionInfo.create(
            permission.getId(),
            permission.getPermissionCode(),
            permission.getPermissionName(),
            permission.getPermissionType(),
            permission.getParentId(),
            permission.getPath(),
            permission.getComponent(),
            permission.getIcon(),
            permission.getSort(),
            permission.getStatus()
        );
    }
} 