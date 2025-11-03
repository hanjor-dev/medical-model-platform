/**
 * 用户管理服务实现类：提供用户管理相关的业务功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:30:00
 */
package com.okbug.platform.service.user.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.utils.PasswordUtils;
import com.okbug.platform.common.utils.ReferralCodeUtils;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;
import com.okbug.platform.dto.user.request.ChangePasswordRequest;
import com.okbug.platform.dto.user.request.UserProfileUpdateRequest;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.auth.RolePermissionMapper;
import com.okbug.platform.mapper.auth.PermissionMapper;
import com.okbug.platform.entity.auth.RolePermission;
import com.okbug.platform.entity.auth.UserPermission;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.mapper.auth.UserPermissionMapper;
import com.okbug.platform.mapper.credit.UserCreditMapper;
import com.okbug.platform.service.credit.CreditService;
import com.okbug.platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final CreditService creditService;
    private final SystemConfigService systemConfigService;
    private final UserPermissionMapper userPermissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserCreditMapper userCreditMapper;
    
    @Override
    /**
     * 获取当前登录用户资料。
     *
     * @return 去除敏感字段后的用户实体
     * @throws ServiceException 当用户不存在时抛出 {@link ErrorCode#USER_NOT_FOUND}
     */
    public User getCurrentUserProfile() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 清除敏感信息
        user.setPassword(null);
        
        log.info("获取用户信息成功，用户ID: {}", currentUserId);
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 修改当前登录用户资料（邮箱、手机号、昵称、头像）。
     *
     * - 当邮箱变更时校验唯一性；空串按 null 处理
     * - 返回更新后的用户并清理密码字段
     *
     * @param request 更新请求
     * @return 更新后的用户
     * @throws ServiceException 用户不存在或邮箱冲突时抛出
     */
    public User updateCurrentUserProfile(UserProfileUpdateRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查邮箱唯一性（如果修改了邮箱）
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // 如果新邮箱不为空，检查唯一性
            if (request.getEmail().trim().length() > 0) {
                checkEmailUniqueness(request.getEmail(), currentUserId);
            }
            user.setEmail(request.getEmail().trim().length() > 0 ? request.getEmail().trim() : null);
        }
        
        // 更新其他信息
        if (request.getPhone() != null) {
            // 如果手机号不为空，设置；如果为空字符串，设置为null
            user.setPhone(request.getPhone().trim().length() > 0 ? request.getPhone().trim() : null);
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        userMapper.updateById(user);
        
        // 重新查询更新后的用户信息
        User updatedUser = userMapper.selectById(currentUserId);
        if (updatedUser != null) {
            // 清除敏感信息
            updatedUser.setPassword(null);
        }
        
        log.info("用户信息修改成功，用户ID: {}", currentUserId);
        return updatedUser;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 修改当前登录用户密码。
     *
     * @param request 包含旧密码与新密码
     * @throws ServiceException 当用户不存在、旧密码错误或新密码强度不足时抛出
     */
    public void changePassword(ChangePasswordRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 验证旧密码
        if (!PasswordUtils.matches(request.getOldPassword(), user.getPassword())) {
            throw new ServiceException(ErrorCode.OLD_PASSWORD_WRONG);
        }
        
        // 验证新密码强度
        if (!PasswordUtils.isStrongPassword(request.getNewPassword())) {
            throw new ServiceException(ErrorCode.PASSWORD_TOO_WEAK);
        }
        
        // 更新密码
        String encodedPassword = PasswordUtils.encodePassword(request.getNewPassword());
        user.setPassword(encodedPassword);
        userMapper.updateById(user);
        
        log.info("用户密码修改成功，用户ID: {}", currentUserId);
    }
    
    @Override
    /**
     * 查询子用户列表（超级管理员支持按 teamId 过滤）。
     *
     * @param page   分页参数
     * @param teamId 团队ID，可为空
     * @return 分页的用户列表（已清理密码）
     * @throws ServiceException 当当前用户不存在或无权限时抛出
     */
    public IPage<User> getSubUsers(Page<User> page, Long teamId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        // 校验当前用户角色
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!currentUser.isSuperAdmin() && !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以查看用户列表");
        }
        
        // SUPER_ADMIN: 支持按团队过滤；不传 teamId 则查询所有账号（排除自己）
        // ADMIN: 兜底（理论上前端不可达）
        IPage<User> result;
        // 使用子查询方式避免直接依赖 TeamMemberMapper
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (teamId != null) {
            // where id in (select user_id from team_members where team_id = ? and status=1 and is_deleted=0)
            query.ne(User::getId, currentUserId)
                 .inSql(User::getId, "select user_id from team_members where team_id = " + teamId + " and status = 1 and is_deleted = 0")
                 .orderByDesc(User::getCreateTime);
        } else {
            query.ne(User::getId, currentUserId)
                 .orderByDesc(User::getCreateTime);
        }
        result = userMapper.selectPage(page, query);
        
        // 清除敏感信息
        result.getRecords().forEach(user -> user.setPassword(null));
        
        log.info("获取用户列表成功(超级管理员)，操作者ID: {}, 记录数: {}", currentUserId, result.getTotal());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 创建子账号。
     *
     * - 校验用户名/邮箱唯一性
     * - 种子化推荐码与初始权限、初始化积分账户（失败仅记录日志）
     *
     * @param request 子账号注册请求
     * @return 注册响应（含新用户ID/用户名/推荐码）
     * @throws ServiceException 无权限或参数冲突时抛出
     */
    public UserRegisterResponse createSubUser(UserRegisterRequest request) {
        ensureSubAccountApiEnabled();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        // 验证当前用户是否为管理员
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以创建子账号");
        }
        
        // 检查用户名唯一性
        checkUsernameUniqueness(request.getUsername());
        
        // 检查邮箱唯一性
        if (StrUtil.isNotBlank(request.getEmail())) {
            checkEmailUniqueness(request.getEmail(), null);
        }
        
        // 生成推荐码
        String referralCode = generateUniqueReferralCode();
        
        // 创建子账号
        User subUser = new User();
        subUser.setUsername(request.getUsername());
        subUser.setEmail(request.getEmail());
        subUser.setPhone(request.getPhone());
        subUser.setPassword(PasswordUtils.encodePassword(request.getPassword()));
        subUser.setNickname(request.getUsername()); // 默认昵称为用户名
        subUser.setStatus(User.STATUS_ENABLED);
        subUser.setRole(User.ROLE_USER); // 子账号默认为普通用户
        subUser.setParentUserId(currentUserId); // 设置父用户ID
        subUser.setReferralCode(referralCode);
        subUser.setLoginFailCount(0);
        
        int result = userMapper.insert(subUser);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "子账号创建失败");
        }
        // 初始化子账号的权限（按默认角色复制到用户-权限表）
        try {
            seedInitialUserPermissions(subUser.getId(), subUser.getRole());
        } catch (Exception e) {
            log.error("初始化子账号权限失败，用户ID: {}，错误: {}", subUser.getId(), e.getMessage(), e);
        }
        
        // 积分系统集成 - 初始化子账号积分
        try {
            creditService.initializeUserCredits(subUser.getId());
            log.info("子账号积分账户初始化成功，用户ID: {}", subUser.getId());
        } catch (Exception e) {
            log.error("子账号积分账户初始化失败，用户ID: {}, 错误: {}", subUser.getId(), e.getMessage());
            // 积分初始化失败不影响用户创建，只记录日志
        }
        
        log.info("子账号创建成功，子账号ID: {}, 父用户ID: {}", subUser.getId(), currentUserId);
        
        return UserRegisterResponse.create(subUser.getId(), subUser.getUsername(), subUser.getReferralCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新子用户资料。
     *
     * @param userId  子用户ID
     * @param request 更新请求
     * @return 更新后的用户（已清理密码）
     * @throws ServiceException 当无权或用户不存在时抛出
     */
    public User updateSubUser(Long userId, UserProfileUpdateRequest request) {
        ensureSubAccountApiEnabled();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 校验当前用户是管理员
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以更新子用户");
        }

        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        // 只能操作自己创建/管理的子用户
        // 超级管理员可更新任意用户；普通管理员只能更新自己名下的子账号
        if (!currentUser.isSuperAdmin() && !currentUserId.equals(target.getParentUserId())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该用户");
        }

        // 邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().equals(target.getEmail())) {
            if (StrUtil.isNotBlank(request.getEmail())) {
                checkEmailUniqueness(request.getEmail(), userId);
            }
            target.setEmail(StrUtil.isNotBlank(request.getEmail()) ? request.getEmail().trim() : null);
        }

        if (request.getPhone() != null) {
            target.setPhone(StrUtil.isNotBlank(request.getPhone()) ? request.getPhone().trim() : null);
        }
        if (request.getNickname() != null) {
            target.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            target.setAvatar(request.getAvatar());
        }

        userMapper.updateById(target);

        User updated = userMapper.selectById(userId);
        if (updated != null) {
            updated.setPassword(null);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除子用户，带会话强制下线与外键清理。
     *
     * @param userId 子用户ID
     * @throws ServiceException 当无权、用户不存在或删除失败时抛出
     */
    public void deleteSubUser(Long userId) {
        ensureSubAccountApiEnabled();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以删除子用户");
        }

        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        // 超级管理员可删除任意用户；普通管理员只能删除自己名下的子账号
        if (!currentUser.isSuperAdmin() && !currentUserId.equals(target.getParentUserId())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该用户");
        }

        // 删除用户前先强制下线并清除会话缓存
        try {
            // 强制指定用户下线，清除其所有会话
            StpUtil.kickout(userId);
            log.info("删除用户前已强制用户下线: userId={}", userId);
        } catch (Exception e) {
            // 记录日志但不影响主流程，因为用户可能本身就没有登录
            log.warn("删除用户时强制下线发生异常: userId={}, error={}", userId, e.getMessage());
        }

        // 先清理该用户的权限覆盖和积分账户等外键数据
        cleanupUserAssociations(userId);

        int affected = userMapper.deleteById(userId);
        if (affected <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "删除用户失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 切换子用户状态（启用/禁用）。禁用后会强制下线。
     *
     * @param userId 子用户ID
     * @param status 目标状态
     * @throws ServiceException 无权、用户不存在或状态无效时抛出
     */
    public void toggleSubUserStatus(Long userId, Integer status) {
        ensureSubAccountApiEnabled();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以修改用户状态");
        }

        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        // 超级管理员可切换任意用户状态；普通管理员只能操作自己名下的子账号
        if (!currentUser.isSuperAdmin() && !currentUserId.equals(target.getParentUserId())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该用户");
        }
        if (status == null || (status != User.STATUS_ENABLED && status != User.STATUS_DISABLED)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "状态值无效");
        }

        // 记录原始状态
        Integer originalStatus = target.getStatus();
        
        // 更新用户状态
        target.setStatus(status);
        userMapper.updateById(target);
        
        // 如果是禁用用户，需要强制下线并清除会话缓存
        if (Integer.valueOf(User.STATUS_DISABLED).equals(status) && Integer.valueOf(User.STATUS_ENABLED).equals(originalStatus)) {
            try {
                // 强制指定用户下线，清除其所有会话
                StpUtil.kickout(userId);
                log.info("用户状态切换为禁用，已强制用户下线: userId={}", userId);
            } catch (Exception e) {
                // 记录日志但不影响主流程，因为用户可能本身就没有登录
                log.warn("强制用户下线时发生异常: userId={}, error={}", userId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 重置子用户密码为系统默认密码。
     *
     * @param userId 子用户ID
     * @return 明文默认密码（仅用于一次性告知）
     * @throws ServiceException 当无权、用户不存在或默认密码不符合强度时抛出
     */
    public String resetSubUserPassword(Long userId) {
        ensureSubAccountApiEnabled();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !currentUser.hasAdminRole()) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "只有管理员可以重置密码");
        }

        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        // 超级管理员可重置任意用户；普通管理员只能重置自己名下的子账号
        if (!currentUser.isSuperAdmin() && !currentUserId.equals(target.getParentUserId())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该用户");
        }
        String defaultPassword = systemConfigService.getConfigValue(SystemConfigKeys.USER_DEFAULT_PASSWORD, SystemConfigKeys.getDefaultValue(SystemConfigKeys.USER_DEFAULT_PASSWORD));
        if (StrUtil.isBlank(defaultPassword) || !PasswordUtils.isStrongPassword(defaultPassword)) {
            throw new ServiceException(ErrorCode.PASSWORD_TOO_WEAK, "系统默认密码不符合强度要求");
        }
        String encoded = PasswordUtils.encodePassword(defaultPassword);
        target.setPassword(encoded);
        userMapper.updateById(target);
        return defaultPassword;
    }
    
    // ================ 私有方法 ================
    
    /**
     * 检查用户名唯一性。
     *
     * @param username 待校验用户名
     * @throws ServiceException 当用户名已存在时抛出 {@link ErrorCode#USERNAME_ALREADY_EXISTS}
     */
    private void checkUsernameUniqueness(String username) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, username);
        
        Long count = userMapper.selectCount(query);
        if (count > 0) {
            throw new ServiceException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }
    
    /**
     * 检查邮箱唯一性。
     *
     * @param email          待校验邮箱
     * @param excludeUserId  更新场景下排除的用户ID
     * @throws ServiceException 当邮箱已存在时抛出 {@link ErrorCode#EMAIL_ALREADY_EXISTS}
     */
    private void checkEmailUniqueness(String email, Long excludeUserId) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getEmail, email);
        if (excludeUserId != null) {
            query.ne(User::getId, excludeUserId);
        }
        
        Long count = userMapper.selectCount(query);
        if (count > 0) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    
    /**
     * 生成唯一推荐码。
     *
     * @return 不重复的推荐码
     */
    private String generateUniqueReferralCode() {
        String referralCode;
        do {
            referralCode = ReferralCodeUtils.generateReferralCode();
            
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            query.eq(User::getReferralCode, referralCode);
            Long count = userMapper.selectCount(query);
            
            if (count == 0) {
                break;
            }
        } while (true);
        
        return referralCode;
    }

    /**
     * 清理用户的关联数据（外键/从表）。
     * - 积分账户（重命名避免唯一冲突后逻辑删除）
     * - 用户权限覆盖
     *
     * @param userId 目标用户ID
     */
    private void cleanupUserAssociations(Long userId) {
        try {
            // 删除积分账户
            try {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.okbug.platform.entity.credit.UserCredit> q1 =
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                q1.eq(com.okbug.platform.entity.credit.UserCredit::getUserId, userId);
                java.util.List<com.okbug.platform.entity.credit.UserCredit> accounts = userCreditMapper.selectList(q1);
                if (accounts != null && !accounts.isEmpty()) {
                    for (com.okbug.platform.entity.credit.UserCredit account : accounts) {
                        try {
                            String suffix = buildDeletedSuffix(account.getId());
                            com.okbug.platform.entity.credit.UserCredit rename = new com.okbug.platform.entity.credit.UserCredit();
                            rename.setId(account.getId());
                            // 避免 (user_id, credit_type_code, is_deleted) 唯一冲突
                            rename.setCreditTypeCode(account.getCreditTypeCode() + suffix);
                            userCreditMapper.updateById(rename);
                            // 再执行逻辑删除
                            userCreditMapper.deleteById(account.getId());
                        } catch (Exception e) {
                            log.warn("软删除用户积分账户失败，userId: {} accountId: {} error: {}", userId, account.getId(), e.getMessage());
                        }
                    }
                }
            } catch (Exception ignored) {}

            // 删除用户权限覆盖
            try {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.okbug.platform.entity.auth.UserPermission> q2 =
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                q2.eq(com.okbug.platform.entity.auth.UserPermission::getUserId, userId);
                userPermissionMapper.delete(q2);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.warn("清理用户关联数据失败，userId: {}", userId, e);
        }
    }

    /**
     * 生成软删除后缀以规避唯一索引冲突。
     *
     * @param id 记录ID
     * @return 用于拼接字段的唯一后缀
     */
    private String buildDeletedSuffix(Long id) {
        String unique = String.valueOf(id != null ? id : System.currentTimeMillis());
        return "__deleted__" + unique;
    }

    /**
     * 根据角色为用户初始化权限到用户-权限关联表（ALLOW）。
     *
     * @param userId 目标用户ID
     * @param role   角色标识
     */
    private void seedInitialUserPermissions(Long userId, String role) {
        if (userId == null || role == null) {
            return;
        }

        // 清理用户现有权限（幂等）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPermission> del = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        del.eq(UserPermission::getUserId, userId);
        userPermissionMapper.delete(del);

        java.util.List<Long> permissionIds;
        if (com.okbug.platform.entity.auth.User.ROLE_SUPER_ADMIN.equals(role)) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission> q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            q.eq(Permission::getStatus, Permission.STATUS_ENABLED);
            java.util.List<Permission> allPerms = permissionMapper.selectList(q);
            permissionIds = allPerms.stream().map(Permission::getId).collect(java.util.stream.Collectors.toList());
        } else {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            q.eq(RolePermission::getRole, role);
            java.util.List<RolePermission> rps = rolePermissionMapper.selectList(q);
            permissionIds = rps.stream().map(RolePermission::getPermissionId).collect(java.util.stream.Collectors.toList());
        }

        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        for (Long pid : permissionIds) {
            if (pid == null) continue;
            UserPermission up = new UserPermission();
            up.setUserId(userId);
            up.setPermissionId(pid);
            up.setGrantType(UserPermission.GRANT_ALLOW);
            userPermissionMapper.insert(up);
        }
    }

    /**
     * 当关闭子账号API时阻止调用，提示迁移至团队成员功能。
     *
     * @throws ServiceException 当开关关闭时抛出 {@link ErrorCode#OPERATION_NOT_ALLOWED}
     */
    private void ensureSubAccountApiEnabled() {
        Boolean enabled = systemConfigService.getConfigValueAsBoolean(
                SystemConfigKeys.SUBACCOUNT_API_ENABLED,
                Boolean.parseBoolean(SystemConfigKeys.getDefaultValue(SystemConfigKeys.SUBACCOUNT_API_ENABLED))
        );
        if (enabled == null || !enabled) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "子账号API已下线，请使用团队成员功能");
        }
    }
} 