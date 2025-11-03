/**
 * 认证服务实现类：实现用户认证相关的业务逻辑
 * 
 * 核心功能：
 * 1. 用户注册业务逻辑（包含推荐码处理，积分部分TODO）
 * 2. 用户登录验证（包含锁定机制）
 * 3. 用户退出登录
 * 4. Token刷新
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:10:00
 */
package com.okbug.platform.service.auth.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.utils.IpUtils;
import com.okbug.platform.common.utils.PasswordUtils;
import com.okbug.platform.common.utils.ReferralCodeUtils;
import com.okbug.platform.common.utils.NicknameUtils;
import com.okbug.platform.dto.auth.request.UserLoginRequest;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserLoginResponse;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;
import com.okbug.platform.dto.auth.response.LoginPermissionInfo;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.auth.RolePermissionMapper;
import com.okbug.platform.mapper.auth.UserPermissionMapper;
import com.okbug.platform.entity.auth.RolePermission;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.entity.auth.UserPermission;
import com.okbug.platform.service.auth.AuthService;
import com.okbug.platform.service.permission.PermissionService;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.service.credit.CreditService;
import com.okbug.platform.service.credit.CreditScenarioService;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.manager.credit.CreditScenarioManager;
 
import com.okbug.platform.service.team.TeamService;
import com.okbug.platform.service.team.TeamMemberService;
import com.okbug.platform.service.team.TeamJoinRequestService;
import com.okbug.platform.service.team.TeamConfigService;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.service.system.message.NotificationFacade;
 
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final SystemConfigService systemConfigService;
    private final CreditService creditService;
    private final CreditScenarioService creditScenarioService;
    private final CreditScenarioManager scenarioManager;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final com.okbug.platform.mapper.auth.PermissionMapper permissionMapper;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final TeamConfigService teamConfigService;
    private final NotificationFacade notificationFacade;
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("用户注册开始，用户名: {}", request.getUsername());
        
        // 1. 检查注册功能是否开启
        checkRegisterEnabled();
        
        // 2. 参数验证和清理
        request.cleanAndNormalize();
        validateRegisterRequest(request);
        
        // 3. 检查用户名和邮箱唯一性
        checkUserUniqueness(request.getUsername(), request.getEmail());
        
        // 4. 验证推荐码
        User referrerUser = validateReferralCode(request.getReferralCode());
        
        // 5. 创建用户记录
        User newUser = createUser(request, referrerUser);
        userMapper.insert(newUser);
        // 6. 初始化用户权限：将角色默认权限写入用户权限表
        try {
            seedInitialUserPermissions(newUser.getId(), newUser.getRole());
        } catch (Exception e) {
            log.error("初始化用户权限失败，用户ID: {}，错误: {}", newUser.getId(), e.getMessage(), e);
        }
        
        // 7. 团队集成（团队码/审批）
        try {
            processTeamJoinOnRegister(newUser, request);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("注册后处理团队逻辑失败，用户ID: {}，错误: {}", newUser.getId(), e.getMessage(), e);
        }

        // 8. 积分系统集成
        processUserRegistrationCredits(newUser, referrerUser);
        
        // 9. 注册成功后的消息通知
        try {
            // 引荐码：通知引荐人
            if (referrerUser != null && referrerUser.getId() != null) { notificationFacade.notifyReferrerOnRegister(referrerUser, newUser); }
        } catch (Exception e) {
            log.warn("注册成功后通知引荐人失败，referrerId={}, newUserId={}, err={}",
                (referrerUser == null ? null : referrerUser.getId()), newUser.getId(), e.getMessage());
        }

		log.info("用户注册成功，用户ID: {}, 用户名: {}", newUser.getId(), newUser.getUsername());

        return UserRegisterResponse.create(newUser.getId(), newUser.getUsername(), newUser.getReferralCode());
    }
    
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("用户登录开始，用户名: {}", request.getUsername());
        
        // 1. 参数验证和清理
        request.cleanAndNormalize();
        
        // 2. 获取客户端信息
        HttpServletRequest httpRequest = getCurrentHttpRequest();
        String clientIp = IpUtils.getClientIp(httpRequest);
        
        try {
            // 3. 获取用户信息
            User user = getUserByUsernameOrEmail(request.getUsername());
            
            // 4. 验证用户状态
            validateUserStatus(user);
            
            // 5. 验证密码
            validatePassword(user, request.getPassword());
            
            // 6. 更新登录信息
            updateLoginInfo(user, clientIp);
            
            // 7. 生成Sa-Token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 8. 获取用户完整权限信息
            List<LoginPermissionInfo> permissions = permissionService.getLoginUserPermissions(user.getId());
            
			log.info("用户登录成功，用户ID: {}, 用户名: {}, 权限数量: {}", user.getId(), user.getUsername(), permissions.size());

			// 登录成功后发送站内消息（失败不影响主流程）
			try { notificationFacade.sendLoginSuccess(user, clientIp); } catch (Exception e) { log.warn("发送登录成功消息失败，用户ID: {}, 错误: {}", user.getId(), e.getMessage()); }

            UserLoginResponse resp = UserLoginResponse.create(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole(), permissions);
            // 附加当前团队（如果存在团队成员记录，选择一条；团队被禁用也应维持会话信息，仅限制后续操作）
            try {
                com.okbug.platform.entity.team.TeamMember m = teamMemberService.getFirstActiveMembership(user.getId());
                if (m != null && m.getTeamId() != null) {
                    Team t = teamService.getById(m.getTeamId());
                    if (t != null && (t.getIsDeleted() == null || t.getIsDeleted() == 0)) {
                        resp.withCurrentTeam(t.getId(), t.getTeamName())
                            .withTeamDisabled(t.getStatus() != null && t.getStatus() == Team.STATUS_DISABLED);
                    } else {
                        // 团队不存在或已被解散：显式清空当前团队信息
                        resp.withCurrentTeam(null, null).withTeamDisabled(null);
                    }
                } else {
                    // 没有有效成员关系：显式清空当前团队信息
                    resp.withCurrentTeam(null, null).withTeamDisabled(null);
                }
            } catch (Exception e) {
                log.warn("获取用户当前团队失败，userId={}, err={}", user.getId(), e.getMessage());
            }
            return resp;
            
        } catch (ServiceException e) {
            log.warn("用户登录失败，用户名: {}, 原因: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            StpUtil.logout();
            log.info("用户退出登录成功，用户ID: {}", userId);
        }
    }
    
    @Override
    public String refreshToken() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        
        // Sa-Token自动续期机制
        StpUtil.renewTimeout(StpUtil.getTokenTimeout());
        String newToken = StpUtil.getTokenValue();
        
        log.info("Token刷新成功，用户ID: {}", userId);
        return newToken;
    }
    
    @Override
    public UserLoginResponse getSessionUser() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        // 用户权限
        List<LoginPermissionInfo> permissions = permissionService.getLoginUserPermissions(userId);
        // 复用当前token
        String token = StpUtil.getTokenValue();
        UserLoginResponse resp = UserLoginResponse.create(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole(), permissions);
        // 当前团队（团队被禁用也应维持会话信息，仅限制后续操作）
        try {
            com.okbug.platform.entity.team.TeamMember m = teamMemberService.getFirstActiveMembership(userId);
            if (m != null && m.getTeamId() != null) {
                Team t = teamService.getById(m.getTeamId());
                if (t != null && (t.getIsDeleted() == null || t.getIsDeleted() == 0)) {
                    resp.withCurrentTeam(t.getId(), t.getTeamName())
                        .withTeamDisabled(t.getStatus() != null && t.getStatus() == Team.STATUS_DISABLED);
                } else {
                    // 团队不存在或已被解散：显式清空当前团队信息
                    resp.withCurrentTeam(null, null).withTeamDisabled(null);
                }
            } else {
                // 没有有效成员关系：显式清空当前团队信息
                resp.withCurrentTeam(null, null).withTeamDisabled(null);
            }
        } catch (Exception e) {
            log.warn("获取会话用户当前团队失败，userId={}, err={}", userId, e.getMessage());
        }
        return resp;
    }
    
    // ================ 私有方法 ================
    /**
     * 根据角色为用户初始化权限到用户-权限关联表（ALLOW）
     */
    private void seedInitialUserPermissions(Long userId, String role) {
        if (userId == null || role == null) {
            return;
        }

        // 先清理该用户现有的权限行（幂等）
        LambdaQueryWrapper<UserPermission> del = new LambdaQueryWrapper<>();
        del.eq(UserPermission::getUserId, userId);
        userPermissionMapper.delete(del);

        List<Long> permissionIds;
        if (com.okbug.platform.entity.auth.User.ROLE_SUPER_ADMIN.equals(role)) {
            // 超级管理员：授予所有启用权限
            LambdaQueryWrapper<Permission> permQuery = new LambdaQueryWrapper<>();
            permQuery.eq(Permission::getStatus, Permission.STATUS_ENABLED);
            List<Permission> allPerms = permissionMapper.selectList(permQuery);
            permissionIds = allPerms.stream().map(Permission::getId).collect(java.util.stream.Collectors.toList());
        } else {
            // 普通角色：复制角色-权限关联
            LambdaQueryWrapper<RolePermission> rpQuery = new LambdaQueryWrapper<>();
            rpQuery.eq(RolePermission::getRole, role);
            List<RolePermission> rps = rolePermissionMapper.selectList(rpQuery);
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
     * 验证注册请求参数
     */
    private void validateRegisterRequest(UserRegisterRequest request) {
        // 验证密码强度
        if (!PasswordUtils.isStrongPassword(request.getPassword())) {
            String description = PasswordUtils.getPasswordStrengthDescription(request.getPassword());
            throw new ServiceException(ErrorCode.PASSWORD_TOO_WEAK, description);
        }
        
        // 验证推荐码格式
        if (request.hasReferralCode() && !ReferralCodeUtils.isValidFormat(request.getReferralCode())) {
            throw new ServiceException(ErrorCode.REFERRAL_CODE_INVALID);
        }
    }
    
    /**
     * 检查用户名和邮箱唯一性
     */
    private void checkUserUniqueness(String username, String email) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, username);
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new ServiceException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        
        // 检查邮箱是否存在（如果提供了邮箱）
        if (email != null && !email.trim().isEmpty()) {
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
            emailQuery.eq(User::getEmail, email);
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }
    }
    
    /**
     * 验证推荐码
     */
    private User validateReferralCode(String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return null;
        }
        
        String cleanCode = ReferralCodeUtils.cleanReferralCode(referralCode);
        if (cleanCode == null) {
            return null;
        }
        
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getReferralCode, cleanCode);
        User referrer = userMapper.selectOne(query);
        
        if (referrer == null) {
            log.warn("推荐码无效: {}", cleanCode);
            return null;
        }
        
        return referrer;
    }
    
    /**
     * 创建用户记录
     */
    private User createUser(UserRegisterRequest request, User referrerUser) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encodePassword(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        // 设置昵称：如果用户填写了昵称则使用，否则自动生成
        String nickname = determineUserNickname(request);
        user.setNickname(nickname);
        
        user.setStatus(User.STATUS_ENABLED);
        user.setRole(User.ROLE_USER);
        user.setLoginFailCount(0);
        
        // 设置推荐关系
        if (referrerUser != null) {
            user.setReferrerUserId(referrerUser.getId());
        }
        
        // 生成唯一推荐码
        String referralCode = ReferralCodeUtils.generateUniqueReferralCode(code -> {
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            query.eq(User::getReferralCode, code);
            return userMapper.selectCount(query) > 0;
        });
        user.setReferralCode(referralCode);
        
        return user;
    }
    
    /**
     * 根据用户名或邮箱获取用户
     */
    private User getUserByUsernameOrEmail(String usernameOrEmail) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        
        if (usernameOrEmail.contains("@")) {
            // 邮箱登录
            query.eq(User::getEmail, usernameOrEmail);
        } else {
            // 用户名登录
            query.eq(User::getUsername, usernameOrEmail);
        }
        
        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        
        return user;
    }
    
    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        // 检查账户是否被禁用
        if (user.isDisabled()) {
            throw new ServiceException(ErrorCode.USER_ACCOUNT_DISABLED);
        }
        
        // 检查登录锁定
        if (user.isLocked()) {
            throw new ServiceException(ErrorCode.USER_ACCOUNT_LOCKED, ErrorCode.USER_ACCOUNT_LOCKED.getMessage() + "，解锁时间：" + user.getLoginLockTime());
        }
    }
    
    /**
     * 验证密码
     */
    private void validatePassword(User user, String password) {
        if (!PasswordUtils.matches(password, user.getPassword())) {
            // 增加登录失败次数
            user.increaseLoginFailCount();
            
            // 检查是否需要锁定账户
            Integer maxLoginFailCount = systemConfigService.getConfigValueAsInt(
                SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            Integer loginLockDuration = systemConfigService.getConfigValueAsInt(
                SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            
            if (user.getLoginFailCount() >= maxLoginFailCount) {
                user.setLoginLock(loginLockDuration);
                log.warn("用户登录失败次数过多，账户已锁定，用户ID: {}, 失败次数: {}, 锁定时长: {}分钟", 
                    user.getId(), user.getLoginFailCount(), loginLockDuration);
            }
            
            // 更新失败次数到数据库
            userMapper.updateById(user);
            
            throw new ServiceException(ErrorCode.USER_PASSWORD_WRONG);
        }
    }
    
    /**
     * 更新登录信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(User user, String loginIp) {
        user.updateLastLogin(loginIp);
        userMapper.updateById(user);
    }
    

    
    /**
     * 确定用户昵称
     * 
     * @param request 注册请求
     * @return 确定的昵称
     */
    private String determineUserNickname(UserRegisterRequest request) {
        // 如果用户填写了昵称，验证并清理后使用
        if (request.hasNickname()) {
            String userNickname = NicknameUtils.cleanNickname(request.getNickname());
            if (userNickname != null) {
                log.info("用户填写昵称: {}", userNickname);
                return userNickname;
            } else {
                log.warn("用户填写的昵称无效，将自动生成昵称");
            }
        }
        
        // 自动生成唯一昵称
        String generatedNickname = NicknameUtils.generateUniqueNickname(nickname -> {
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            query.eq(User::getNickname, nickname);
            return userMapper.selectCount(query) > 0;
        });
        
        log.info("自动生成用户昵称: {}", generatedNickname);
        return generatedNickname;
    }
    
    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    
    /**
     * 检查用户注册功能是否开启
     */
    private void checkRegisterEnabled() {
        Boolean registerEnabled = systemConfigService.getConfigValueAsBoolean(
            SystemConfigKeys.USER_REGISTER_ENABLED, true);
        
        if (Boolean.FALSE.equals(registerEnabled)) {
            log.warn("用户注册功能已关闭，拒绝注册请求");
            throw new ServiceException(ErrorCode.USER_REGISTER_DISABLED);
        }
    }
    
    /**
     * 处理用户注册相关的积分逻辑
     * 
     * @param newUser 新注册用户
     * @param referrerUser 推荐人用户（可为null）
     */
    private void processUserRegistrationCredits(User newUser, User referrerUser) {
        try {
            // 1. 初始化用户积分账户
            log.info("初始化用户积分账户，用户ID: {}", newUser.getId());
            creditService.initializeUserCredits(newUser.getId());
            
            // 2. 检查是否开启注册奖励
            Boolean registerRewardEnabled = systemConfigService.getConfigValueAsBoolean(
                SystemConfigKeys.USER_REGISTER_REWARD_ENABLED, true);
            
            if (Boolean.TRUE.equals(registerRewardEnabled)) {
                // 发放注册奖励积分
                processRegistrationReward(newUser);
            } else {
                log.info("用户注册奖励功能已关闭，跳过注册奖励发放，用户ID: {}", newUser.getId());
            }
            
            // 3. 检查是否开启推荐奖励
            Boolean referralRewardEnabled = systemConfigService.getConfigValueAsBoolean(
                SystemConfigKeys.USER_REFERRAL_REWARD_ENABLED, true);
            
            if (Boolean.TRUE.equals(referralRewardEnabled) && referrerUser != null) {
                // 发放推荐奖励积分
                processReferralReward(referrerUser, newUser);
            } else if (referrerUser != null) {
                log.info("引荐奖励功能已关闭，跳过推荐奖励发放，推荐人ID: {}, 新用户ID: {}", 
                    referrerUser.getId(), newUser.getId());
            }
            
        } catch (Exception e) {
            log.error("处理用户注册积分逻辑时发生异常，用户ID: {}, 错误: {}", newUser.getId(), e.getMessage(), e);
            // 积分系统异常不影响用户注册，只记录日志
        }
    }

    /**
     * 处理注册后的团队加入逻辑
     */
    private void processTeamJoinOnRegister(User newUser, com.okbug.platform.dto.auth.request.UserRegisterRequest request) {
        if (request == null || !request.hasTeamCode()) {
            return;
        }
        String teamCode = request.getTeamCode();
        Team team = teamService.findByTeamCode(teamCode);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_CODE_INVALID);
        }
        if (team.getStatus() != null && team.getStatus() == Team.STATUS_DISABLED) {
            log.warn("注册加入团队失败：团队已禁用，teamCode={}, teamId={}, userId={}", teamCode, team.getId(), newUser.getId());
            try {
                String content = "您填写的团队当前处于禁用状态，暂无法加入。团队码：" + teamCode;
                notificationFacade.sendSystemMessageFromEvent(newUser.getId(), "加入团队失败", content, newUser.getId());
            } catch (Exception ex) {
                log.warn("通知注册用户团队禁用失败，userId={}, err={}", newUser.getId(), ex.getMessage());
            }
            return; // 不影响注册主流程
        }
        // 已是成员直接返回
        if (teamMemberService.isMember(team.getId(), newUser.getId())) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
        // 审批开关
        boolean requireApproval = teamConfigService.isJoinApprovalRequired(team.getId());
        if (requireApproval) {
            if (teamJoinRequestService.existsPending(team.getId(), newUser.getId())) {
                throw new ServiceException(ErrorCode.TEAM_JOIN_REQUEST_ALREADY_EXISTS);
            }
            teamJoinRequestService.createPending(team.getId(), newUser.getId(), teamCode, "用户注册加入团队");
            log.info("用户注册创建团队加入申请：teamId={}, userId={}", team.getId(), newUser.getId());
            // 通知团队Owner：有新的加入申请
            try {
                notificationFacade.notifyTeamOwnerOnRegister(team, newUser, true);
            } catch (Exception e) {
                log.warn("发送团队加入申请通知失败，teamId={}, ownerId={}, userId={}, err={}",
                    team.getId(), team.getOwnerUserId(), newUser.getId(), e.getMessage());
            }
        } else {
            teamMemberService.addMemberIfAbsent(team.getId(), newUser.getId());
            log.info("用户注册直接加入团队：teamId={}, userId={}", team.getId(), newUser.getId());
            // 通知团队Owner：有新成员加入
            try {
                notificationFacade.notifyTeamOwnerOnRegister(team, newUser, false);
            } catch (Exception e) {
                log.warn("发送团队新成员加入通知失败，teamId={}, ownerId={}, userId={}, err={}",
                    team.getId(), team.getOwnerUserId(), newUser.getId(), e.getMessage());
            }
        }
    }

    // 移除不再需要的私有查找方法
    
    /**
     * 处理注册奖励积分
     * 
     * @param newUser 新注册用户
     */
    private void processRegistrationReward(User newUser) {
        try {
            // 获取所有奖励场景
            List<CreditUsageScenario> rewardScenarios = creditScenarioService.getRewardScenarios();
            
            for (CreditUsageScenario scenario : rewardScenarios) {
                // 检查是否为注册奖励场景
                if ("USER_REGISTER".equals(scenario.getScenarioCode())) {
                    // 检查用户角色是否有权限
                    if (scenario.hasRolePermission(newUser.getRole())) {
                        // 获取奖励积分数（取绝对值，因为奖励场景的costPerUse为负数）
                        BigDecimal rewardAmount = scenario.getRewardAmount();
                        
                        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
                            // 发放注册奖励积分
                            creditService.rewardCredits(newUser.getId(), scenario.getScenarioCode(), rewardAmount);
                            log.info("发放注册奖励积分成功，用户ID: {}, 场景: {}, 积分数: {}", 
                                newUser.getId(), scenario.getScenarioCode(), rewardAmount);
                        }
                    }
                    break; // 找到注册奖励场景后退出循环
                }
            }
        } catch (Exception e) {
            log.error("处理注册奖励积分时发生异常，用户ID: {}, 错误: {}", newUser.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理推荐奖励积分
     * 
     * @param referrerUser 推荐人用户
     * @param newUser 新注册用户
     */
    private void processReferralReward(User referrerUser, User newUser) {
        try {
            // 直接从场景管理器获取推荐奖励配置
            CreditUsageScenario scenario = scenarioManager.getUserReferralConfig();
            
            if (scenario == null) {
                log.warn("推荐奖励场景配置不存在或已禁用");
                return;
            }
            
            // 检查推荐人角色是否有权限
            if (!scenario.hasRolePermission(referrerUser.getRole())) {
                log.warn("推荐人角色无权限获得推荐奖励，推荐人ID: {}, 角色: {}, 场景: {}", 
                    referrerUser.getId(), referrerUser.getRole(), scenario.getScenarioCode());
                return;
            }
            
            // 获取奖励积分数（取绝对值，因为奖励场景的costPerUse为负数）
            BigDecimal rewardAmount = scenario.getRewardAmount();
            
            if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("推荐奖励积分数无效，推荐人ID: {}, 场景: {}, 积分数: {}", 
                    referrerUser.getId(), scenario.getScenarioCode(), rewardAmount);
                return;
            }
            
            // 发放推荐奖励积分（记录新用户ID作为关联用户）
            creditService.rewardCredits(referrerUser.getId(), scenario.getScenarioCode(), rewardAmount, newUser.getId());
            log.info("发放推荐奖励积分成功，推荐人ID: {}, 新用户ID: {}, 场景: {}, 积分数: {}", 
                referrerUser.getId(), newUser.getId(), scenario.getScenarioCode(), rewardAmount);
            
        } catch (Exception e) {
            log.error("处理推荐奖励积分时发生异常，推荐人ID: {}, 新用户ID: {}, 错误: {}", 
                referrerUser.getId(), newUser.getId(), e.getMessage(), e);
        }
    }

    /**
     * 注册成功后，通知引荐人
     */
    

    /**
     * 注册流程中使用团队码时，通知团队Owner
     *
     * @param team 目标团队
     * @param newUser 新注册用户
     * @param requireApproval 是否需要审批
     */
    
} 