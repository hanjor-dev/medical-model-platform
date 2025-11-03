package com.okbug.platform.service.credit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.entity.credit.CreditRedeemCode;
import com.okbug.platform.entity.credit.CreditTransaction;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.mapper.credit.CreditRedeemCodeMapper;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.mapper.credit.UserCreditMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.service.credit.CreditRedeemCodeService;
import com.okbug.platform.common.enums.credit.CreditTransactionType;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.service.security.BruteForceGuard;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.service.system.message.NotificationFacade;
import com.okbug.platform.common.cache.PermissionCacheService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditRedeemCodeServiceImpl implements CreditRedeemCodeService {
    /**
     * 积分兑换码服务实现
     * 职责：
     * - 生成兑换码（校验积分类型、参数合法性）
     * - 兑换码分页查询（支持关键字与状态过滤，并补齐历史显示名）
     * - 执行兑换（抗暴力尝试、账户校验、交易记录、通知推送）
     * - 查询兑换码信息（含安全策略与状态校验）
     * 
     * 关键约束与安全：
     * - 使用 ServiceException + ErrorCode 进行业务异常统一管理
     * - 关键失败路径配合 BruteForceGuard 进行失败次数记录与临时锁定
     * - 所有方法均在必要位置记录日志，敏感信息（如 codeKey）按需脱敏
     */

    private final CreditRedeemCodeMapper redeemCodeMapper;
    private final CreditTypeMapper creditTypeMapper;
    private final UserCreditMapper userCreditMapper;
    private final CreditTransactionMapper creditTransactionMapper;
    private final UserMapper userMapper;
    private final BruteForceGuard bruteForceGuard;
    private final SystemConfigService systemConfigService;
    
    private final NotificationFacade notificationFacade;
    private final PermissionCacheService permissionCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 生成积分兑换码。
	 *
	 * 事务：是。
	 * 规则：校验积分类型启用；校验创建者存在对应积分账户；生成唯一 codeKey 并入库。
	 * 审计：记录操作者与关键参数；失败路径记 warn/error。
	 *
	 * @param creditTypeCode 积分类型编码，必填
	 * @param amount 数量，>0
	 * @param expireTime 过期时间，可空
	 * @param remark 备注，可空
	 * @return 兑换码实体
	 * @throws ServiceException PARAM_INVALID|CREDIT_TYPE_DISABLED|USER_CREDIT_ACCOUNT_NOT_FOUND|REDEEM_CODE_GENERATION_FAILED
	 */
    public CreditRedeemCode generate(String creditTypeCode, BigDecimal amount, LocalDateTime expireTime, String remark) {
        // 入参与操作者信息
        Long currentUserId = StpUtil.getLoginIdAsLong();
        String currentUsername = getUsername(currentUserId);

        log.info("开始生成兑换码，请求人: {}({})，类型: {}，数量: {}，过期: {}", currentUsername, currentUserId, creditTypeCode, amount, expireTime);

        if (!StringUtils.hasText(creditTypeCode) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("生成兑换码参数非法，creditTypeCode={}，amount={}", creditTypeCode, amount);
            throw new ServiceException(ErrorCode.PARAM_INVALID, "积分类型与数量必须有效");
        }

        // 校验积分类型启用
        CreditType type = creditTypeMapper.selectOne(new LambdaQueryWrapper<CreditType>()
                .eq(CreditType::getTypeCode, creditTypeCode));
        if (type == null || !type.isEnabled()) {
            log.warn("积分类型不可用，typeCode={}", creditTypeCode);
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED);
        }

        // 确保存在用户积分账户
        UserCredit creatorCredit = userCreditMapper.selectOne(new LambdaQueryWrapper<UserCredit>()
                .eq(UserCredit::getUserId, currentUserId)
                .eq(UserCredit::getCreditTypeCode, creditTypeCode));
        if (creatorCredit == null) {
            log.warn("创建者缺少对应积分账户，userId={}，typeCode={}", currentUserId, creditTypeCode);
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND, "创建者缺少对应积分账户");
        }

        // 生成兑换码KEY（简单实现：时间戳+随机，生产中应替换为加密签名方案）
        String codeKey = java.util.UUID.randomUUID().toString().replace("-", "")
                + Long.toString(System.currentTimeMillis(), 36).toUpperCase();

        CreditRedeemCode code = new CreditRedeemCode();
        code.setCodeKey(codeKey);
        code.setCreditTypeCode(creditTypeCode);
        code.setAmount(amount);
        code.setStatus(0);
        code.setExpireTime(expireTime);
        code.setRemark(remark);
        code.setCreatedBy(currentUserId);
        code.setCreatedByName(currentUsername);
        code.setVersion(0);

        int ins = redeemCodeMapper.insert(code);
        if (ins <= 0) {
            log.error("兑换码落库失败，userId={}，typeCode={}", currentUserId, creditTypeCode);
            throw new ServiceException(ErrorCode.REDEEM_CODE_GENERATION_FAILED);
        }

        log.info("兑换码生成成功，创建人: {}({})，类型: {}, 数量: {}, 过期: {}", currentUsername, currentUserId, creditTypeCode, amount, expireTime);
        return code;
    }

    @Override
	/**
	 * 兑换码分页查询（支持关键词与状态过滤）。
	 *
	 * 行为：根据关键词在 codeKey/remark/createdByName/redeemedByName 模糊匹配；
	 * 返回结果中对历史数据的 createdByName/redeemedByName 进行补齐。
	 *
	 * @param page 分页对象
	 * @param keyword 关键词，可空
	 * @param status 状态，可空（0未用/1已用）
	 * @return 分页结果
	 */
    public IPage<CreditRedeemCode> page(Page<CreditRedeemCode> page, String keyword, Integer status) {
        log.debug("查询兑换码分页：pageNo={}，pageSize={}，keyword='{}'，status={}", page.getCurrent(), page.getSize(), keyword, status);
        LambdaQueryWrapper<CreditRedeemCode> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(CreditRedeemCode::getCodeKey, keyword)
              .or().like(CreditRedeemCode::getRemark, keyword)
              .or().like(CreditRedeemCode::getCreatedByName, keyword)
              .or().like(CreditRedeemCode::getRedeemedByName, keyword);
        }
        if (status != null) {
            qw.eq(CreditRedeemCode::getStatus, status);
        }
        qw.orderByDesc(CreditRedeemCode::getCreateTime);
        IPage<CreditRedeemCode> result = redeemCodeMapper.selectPage(page, qw);

        // 补齐历史数据的用户名显示（createdByName / redeemedByName 为空或为纯数字时）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> needUserIds = new java.util.HashSet<>();
            for (CreditRedeemCode r : result.getRecords()) {
                if ((r.getCreatedByName() == null || r.getCreatedByName().matches("^\\d+$")) && r.getCreatedBy() != null) {
                    needUserIds.add(r.getCreatedBy());
                }
                if ((r.getRedeemedByName() == null || r.getRedeemedByName().matches("^\\d+$")) && r.getRedeemedBy() != null) {
                    needUserIds.add(r.getRedeemedBy());
                }
            }
            if (!needUserIds.isEmpty()) {
                java.util.List<User> users = userMapper.selectBatchIds(needUserIds);
                java.util.Map<Long, String> id2name = new java.util.HashMap<>();
                if (users != null) {
                    for (User u : users) {
                        if (u == null) continue;
                        String name = org.springframework.util.StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername();
                        if (!org.springframework.util.StringUtils.hasText(name)) name = String.valueOf(u.getId());
                        id2name.put(u.getId(), name);
                    }
                }
                for (CreditRedeemCode r : result.getRecords()) {
                    if (r.getCreatedBy() != null && (r.getCreatedByName() == null || r.getCreatedByName().matches("^\\d+$"))) {
                        String name = id2name.get(r.getCreatedBy());
                        if (name != null) r.setCreatedByName(name);
                    }
                    if (r.getRedeemedBy() != null && (r.getRedeemedByName() == null || r.getRedeemedByName().matches("^\\d+$"))) {
                        String name = id2name.get(r.getRedeemedBy());
                        if (name != null) r.setRedeemedByName(name);
                    }
                }
            }
        }
        log.debug("兑换码分页查询完成，total={}", result == null ? 0 : result.getTotal());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 执行兑换。
	 *
	 * 安全：集成暴力尝试防护（BruteForceGuard），在多次失败后临时锁定并强制下线；
	 * 合法性：校验兑换码存在/未用/未过期、类型启用、用户账户存在；
	 * 账务：增加余额、标记兑换码已用、写入交易流水；成功后重置失败计数。
	 *
	 * @param codeKey 兑换码 Key，必填
	 * @return 是否兑换成功
	 * @throws ServiceException REDEEM_USER_TEMP_LOCKED|PARAM_MISSING|REDEEM_CODE_INVALID|REDEEM_CODE_USED|REDEEM_CODE_EXPIRED|USER_CREDIT_ACCOUNT_NOT_FOUND|TRANSACTION_FAILED|CREDIT_TRANSACTION_FAILED
	 */
    public boolean redeem(String codeKey) {
        String maskedKey = codeKey == null ? "-" : (codeKey.length() <= 6 ? codeKey : ("***" + codeKey.substring(codeKey.length() - 6)));
        Long userId = StpUtil.getLoginIdAsLong();
        String username = getUsername(userId);

        log.info("发起兑换，用户: {}({})，codeKey={}（已脱敏）", username, userId, maskedKey);

        // 暴力尝试保护：检查是否被锁
        if (bruteForceGuard.isLocked("redeem", userId)) {
            log.warn("用户处于临时锁定状态，userId={}", userId);
            forceOfflineAndClearCache(userId);
            persistUserTempLock(userId);
            throw new ServiceException(ErrorCode.REDEEM_USER_TEMP_LOCKED);
        }

        if (!StringUtils.hasText(codeKey)) {
            log.warn("兑换码为空，userId={}", userId);
            throw new ServiceException(ErrorCode.PARAM_MISSING, "兑换码不能为空");
        }

        CreditRedeemCode code = redeemCodeMapper.selectOne(new LambdaQueryWrapper<CreditRedeemCode>()
                .eq(CreditRedeemCode::getCodeKey, codeKey));
        if (code == null || Objects.equals(code.getIsDeleted(), 1)) {
            int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            int windowMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES, 10);
            bruteForceGuard.recordFailure("redeem", userId,
                    new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(Math.max(windowMin, 0)), java.time.Duration.ofMinutes(lockMin), true));
            if (bruteForceGuard.isLocked("redeem", userId)) {
                forceOfflineAndClearCache(userId);
                persistUserTempLock(userId);
            }
            log.warn("兑换码不存在或已删除，userId={}，codeKey={}（已脱敏）", userId, maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_INVALID);
        }
        if (code.getStatus() != null && code.getStatus() != 0) {
            if (code.getStatus() == 1) {
                int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
                int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
                int windowMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES, 10);
                bruteForceGuard.recordFailure("redeem", userId,
                        new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(Math.max(windowMin, 0)), java.time.Duration.ofMinutes(lockMin), false));
                if (bruteForceGuard.isLocked("redeem", userId)) {
                    forceOfflineAndClearCache(userId);
                    persistUserTempLock(userId);
                }
                log.warn("兑换码已被使用，userId={}，codeKey={}（已脱敏）", userId, maskedKey);
                throw new ServiceException(ErrorCode.REDEEM_CODE_USED);
            }
            log.warn("兑换码状态非法，userId={}，status={}，codeKey={}（已脱敏）", userId, code.getStatus(), maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_INVALID);
        }
        if (code.getExpireTime() != null && code.getExpireTime().isBefore(LocalDateTime.now())) {
            int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            int windowMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES, 10);
            bruteForceGuard.recordFailure("redeem", userId,
                    new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(Math.max(windowMin, 0)), java.time.Duration.ofMinutes(lockMin), false));
            if (bruteForceGuard.isLocked("redeem", userId)) {
                forceOfflineAndClearCache(userId);
                persistUserTempLock(userId);
            }
            log.warn("兑换码已过期，userId={}，codeKey={}（已脱敏）", userId, maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_EXPIRED);
        }

        // 确保用户有对应积分账户
        UserCredit userCredit = userCreditMapper.selectOne(new LambdaQueryWrapper<UserCredit>()
                .eq(UserCredit::getUserId, userId)
                .eq(UserCredit::getCreditTypeCode, code.getCreditTypeCode()));
        if (userCredit == null) {
            int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            int windowMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES, 10);
            bruteForceGuard.recordFailure("redeem", userId,
                    new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(Math.max(windowMin, 0)), java.time.Duration.ofMinutes(lockMin), false));
            if (bruteForceGuard.isLocked("redeem", userId)) {
                forceOfflineAndClearCache(userId);
                persistUserTempLock(userId);
            }
            log.warn("用户积分账户不存在，userId={}，typeCode={}", userId, code.getCreditTypeCode());
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }

        // 加余额（乐观锁）
        java.math.BigDecimal before = userCredit.getAvailableBalance();
        userCredit.addBalance(code.getAmount());
        int up = userCreditMapper.updateById(userCredit);
        if (up <= 0) {
            log.error("账户更新失败（加余额），userId={}，typeCode={}，amount={}", userId, code.getCreditTypeCode(), code.getAmount());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "账户更新失败");
        }

        // 标记兑换码已使用
        code.setStatus(1);
        code.setRedeemedBy(userId);
        code.setRedeemedByName(username);
        code.setRedeemedTime(LocalDateTime.now());
        redeemCodeMapper.updateById(code);

        // 写入积分交易记录（收入）
        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        // 设置用户交易主体
        tx.setUserSubject(userId);
        tx.setCreditTypeCode(code.getCreditTypeCode());
        tx.setTransactionType(CreditTransactionType.EARN.getCode());
        tx.setAmount(code.getAmount());
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(userCredit.getAvailableBalance());
        tx.setRelatedUserId(code.getCreatedBy());
        tx.setRelatedOrderId(code.getCodeKey());
        tx.setScenarioCode(CreditScenarioCode.REDEEM_CODE.getCode());
        tx.setDescription("兑换码兑换: " + code.getCodeKey());

        int inserted = creditTransactionMapper.insert(tx);
        if (inserted <= 0) {
            log.error("记录交易失败，userId={}，typeCode={}，amount={}，codeKey={}（已脱敏）", userId, code.getCreditTypeCode(), code.getAmount(), maskedKey);
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED, "记录交易失败");
        }

        log.info("兑换成功，用户: {}({})，类型: {}, 数量: {}, 余额: {} -> {}",
                username, userId, code.getCreditTypeCode(), code.getAmount(), before, userCredit.getAvailableBalance());
        
        // 发送站内消息通知（不影响主流程）
        try {
            CreditType ct = creditTypeMapper.selectOne(new LambdaQueryWrapper<CreditType>()
                    .eq(CreditType::getTypeCode, code.getCreditTypeCode()));
            String unitName = ct == null || ct.getUnitName() == null ? "" : ct.getUnitName();
            String typeName = ct == null || ct.getTypeName() == null ? code.getCreditTypeCode() : ct.getTypeName();
            notificationFacade.notifyRedeemCodeEarned(userId, code.getCodeKey(), typeName, unitName, code.getAmount(), before, userCredit.getAvailableBalance());
        } catch (Exception e) {
            log.warn("发送兑换到账消息失败，不影响业务，userId={}, codeKey={}, error={}", userId, code.getCodeKey(), e.getMessage());
        }
        // 成功后重置失败计数
        bruteForceGuard.reset("redeem", userId);
        return true;
    }

    @Override
	/**
	 * 查询兑换码信息（预览）。
	 *
	 * 安全：与兑换流程一致的失败计数与临时锁定策略（只在非法/过期/已用等情况下记录失败）。
	 *
	 * @param codeKey 兑换码 Key
	 * @return 兑换码实体（未使用且未过期）
	 * @throws ServiceException REDEEM_USER_TEMP_LOCKED|PARAM_MISSING|REDEEM_CODE_INVALID|REDEEM_CODE_USED|REDEEM_CODE_EXPIRED|CREDIT_TYPE_DISABLED
	 */
    public CreditRedeemCode getInfo(String codeKey) {
        String maskedKey = codeKey == null ? "-" : (codeKey.length() <= 6 ? codeKey : ("***" + codeKey.substring(codeKey.length() - 6)));
        log.info("查询兑换码信息，codeKey={}（已脱敏）", maskedKey);
        Long userId = StpUtil.getLoginIdAsLong();
        if (bruteForceGuard.isLocked("redeem", userId)) {
            // 已被锁定：清理缓存并强制下线
            log.warn("用户处于临时锁定状态，userId={}", userId);
            forceOfflineAndClearCache(userId);
            persistUserTempLock(userId);
            throw new ServiceException(ErrorCode.REDEEM_USER_TEMP_LOCKED);
        }
        if (!StringUtils.hasText(codeKey)) {
            log.warn("兑换码为空，userId={}", userId);
            throw new ServiceException(ErrorCode.PARAM_MISSING, "兑换码不能为空");
        }
        CreditRedeemCode code = redeemCodeMapper.selectOne(new LambdaQueryWrapper<CreditRedeemCode>()
                .eq(CreditRedeemCode::getCodeKey, codeKey));
        if (code == null || java.util.Objects.equals(code.getIsDeleted(), 1)) {
            int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            int windowMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES, 10);
            bruteForceGuard.recordFailure("redeem", userId,
                    new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(Math.max(windowMin, 0)), java.time.Duration.ofMinutes(lockMin), true));
            if (bruteForceGuard.isLocked("redeem", userId)) {
                forceOfflineAndClearCache(userId);
                persistUserTempLock(userId);
            }
            log.warn("兑换码不存在或已删除，userId={}，codeKey={}（已脱敏）", userId, maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_INVALID);
        }
        if (code.getStatus() != null && code.getStatus() != 0) {
            if (code.getStatus() == 1) {
                int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
                int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
                bruteForceGuard.recordFailure("redeem", userId,
                        new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(10), java.time.Duration.ofMinutes(lockMin), false));
                if (bruteForceGuard.isLocked("redeem", userId)) {
                    forceOfflineAndClearCache(userId);
                    persistUserTempLock(userId);
                }
                log.warn("兑换码已被使用（查询）userId={}，codeKey={}（已脱敏）", userId, maskedKey);
                throw new ServiceException(ErrorCode.REDEEM_CODE_USED);
            }
            log.warn("兑换码状态非法（查询）userId={}，status={}，codeKey={}（已脱敏）", userId, code.getStatus(), maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_INVALID);
        }
        if (code.getExpireTime() != null && code.getExpireTime().isBefore(LocalDateTime.now())) {
            int max = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            bruteForceGuard.recordFailure("redeem", userId,
                    new BruteForceGuard.Policy(max, java.time.Duration.ofMinutes(10), java.time.Duration.ofMinutes(lockMin), false));
            if (bruteForceGuard.isLocked("redeem", userId)) {
                forceOfflineAndClearCache(userId);
                persistUserTempLock(userId);
            }
            log.warn("兑换码已过期（查询）userId={}，codeKey={}（已脱敏）", userId, maskedKey);
            throw new ServiceException(ErrorCode.REDEEM_CODE_EXPIRED);
        }
        // 校验积分类型启用
        CreditType type = creditTypeMapper.selectOne(new LambdaQueryWrapper<CreditType>()
                .eq(CreditType::getTypeCode, code.getCreditTypeCode()));
        if (type == null || !type.isEnabled()) {
            log.warn("积分类型不可用（查询），typeCode={}，codeKey={}（已脱敏）", code.getCreditTypeCode(), maskedKey);
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED);
        }
        log.debug("兑换码信息有效，typeCode={}，amount={}", code.getCreditTypeCode(), code.getAmount());
        return code;
    }

	/**
	 * 强制下线并清理权限缓存（安全触发）。
	 *
	 * @param userId 用户ID
	 */
    private void forceOfflineAndClearCache(Long userId) {
        log.debug("开始执行强制下线与清理缓存，userId={}", userId);
        try { permissionCacheService.clearUserPermissions(userId); } catch (Exception ignored) {}
        try { StpUtil.logout(userId); } catch (Exception ignored) {}
    }

	/**
	 * 持久化用户临时锁定标志（用于前端提示与后续解锁）。
	 *
	 * @param userId 用户ID
	 */
    private void persistUserTempLock(Long userId) {
        try {
            int lockMin = systemConfigService.getConfigValueAsInt(SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
            User u = userMapper.selectById(userId);
            if (u != null) {
                u.setLoginLock(lockMin);
                userMapper.updateById(u);
            }
        } catch (Exception ignored) {}
    }

	/**
	 * 获取用户展示名：优先昵称，其次用户名；失败则返回 ID 字符串。
	 *
	 * @param userId 用户ID
	 * @return 展示名
	 */
    private String getUsername(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                if (org.springframework.util.StringUtils.hasText(user.getNickname())) {
                    return user.getNickname();
                }
                if (org.springframework.util.StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
            }
        } catch (Exception ignored) {}
        return String.valueOf(userId);
    }
}


