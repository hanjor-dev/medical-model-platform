/**
 * 积分核心业务Service实现类：实现积分系统的核心业务操作
 * 
 * 功能描述：
 * 1. 积分余额验证和扣减
 * 2. 积分奖励和退款
 * 3. 积分账户初始化和管理
 * 4. 积分转账和分配
 * 5. 积分规则查询和验证
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:35:00
 */
package com.okbug.platform.service.credit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.credit.response.CreditBalanceResponse;
import com.okbug.platform.dto.credit.response.UserCreditsSummaryResponse;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.credit.CreditTransaction;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;
import com.okbug.platform.common.enums.credit.CreditTransactionType;

import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.mapper.credit.CreditUsageScenarioMapper;
import com.okbug.platform.mapper.credit.UserCreditMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.service.credit.CreditService;
import com.okbug.platform.service.system.message.NotificationFacade;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {
    
    private static final int MAX_CONSUME_RETRIES = 3;

    private final UserCreditMapper userCreditMapper;
    private final CreditTypeMapper creditTypeMapper;
    private final CreditUsageScenarioMapper creditScenarioMapper;
    private final CreditTransactionMapper creditTransactionMapper;
    private final UserMapper userMapper;
    
    private final NotificationFacade notificationFacade;
    
    
    @Override
    public boolean validateBalance(String scenarioCode, BigDecimal amount) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("验证用户积分余额，用户ID: {}, 场景编码: {}, 需要积分数: {}", userId, scenarioCode, amount);
        
        if (!StringUtils.hasText(scenarioCode) || amount == null) {
            return false;
        }
        
        // 获取使用场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            log.warn("使用场景不存在或已禁用，场景编码: {}", scenarioCode);
            return false;
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            log.warn("积分类型不存在或已禁用，类型编码: {}", scenario.getCreditTypeCode());
            return false;
        }
        
        // 获取用户积分账户
        UserCredit userCredit = getUserCreditByUserIdAndType(userId, scenario.getCreditTypeCode());
        if (userCredit == null) {
            log.warn("用户积分账户不存在，用户ID: {}, 积分类型: {}", userId, scenario.getCreditTypeCode());
            return false;
        }
        
        // 验证余额是否足够
        return userCredit.hasSufficientBalance(amount);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit consumeCredits(String scenarioCode, BigDecimal amount, String orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("消费积分，用户ID: {}, 场景编码: {}, 积分数: {}, 订单号: {}", userId, scenarioCode, amount, orderId);
        
        // 参数验证增强
        if (!StringUtils.hasText(scenarioCode) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "场景编码不能为空，积分数必须大于0");
        }
        
        // 获取使用场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_DISABLED, "使用场景不存在或已禁用");
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED);
        }
        
        // 验证场景配置的合理性
        if (scenario.getCostPerUse().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.BUSINESS_RULE_VIOLATION, "每次使用消耗积分必须大于0");
        }
        
        // 验证用户是否有权限使用此场景
        if (!canUserUseScenario(scenarioCode)) {
            throw new ServiceException(ErrorCode.CREDIT_USER_ROLE_NOT_ALLOWED);
        }
        
        // 检查每日使用限制
        if (isDailyLimitExceeded(scenarioCode)) {
            throw new ServiceException(ErrorCode.CREDIT_DAILY_LIMIT_EXCEEDED);
        }
        
        // 获取用户积分账户
        UserCredit userCredit = getUserCreditByUserIdAndType(userId, scenario.getCreditTypeCode());
        if (userCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }
        
        // 幂等性：如提供订单号，且已有同用户相同订单的消费交易，则直接返回账户快照
        if (StringUtils.hasText(orderId)) {
            LambdaQueryWrapper<CreditTransaction> idemQuery = new LambdaQueryWrapper<>();
            idemQuery.eq(CreditTransaction::getUserId, userId)
                     .eq(CreditTransaction::getRelatedOrderId, orderId)
                     .eq(CreditTransaction::getTransactionType, CreditTransactionType.SPEND.getCode())
                     .eq(CreditTransaction::getIsDeleted, 0);
            Long existed = creditTransactionMapper.selectCount(idemQuery);
            if (existed != null && existed > 0) {
                log.info("积分消费幂等命中，直接返回账户快照，userId={}, scenarioCode={}, orderId={}", userId, scenarioCode, orderId);
                return userCredit;
            }
        }

        // 验证余额是否足够（初次检查）
        if (!userCredit.hasSufficientBalance(amount)) {
            throw new ServiceException(ErrorCode.INSUFFICIENT_CREDIT_BALANCE);
        }

        // 扣减积分（使用乐观锁，带有限次重试）
        BigDecimal balanceBefore = userCredit.getAvailableBalance();
        BigDecimal balanceAfter = balanceBefore;
        int updateResult = 0;
        int retryCount = 0;
        while (updateResult <= 0 && retryCount < MAX_CONSUME_RETRIES) {
            if (retryCount > 0) {
                // 重新获取最新账户并再次校验余额
                UserCredit latest = getUserCreditByUserIdAndType(userId, scenario.getCreditTypeCode());
                if (latest == null) {
                    throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
                }
                userCredit = latest;
                if (!userCredit.hasSufficientBalance(amount)) {
                    throw new ServiceException(ErrorCode.INSUFFICIENT_CREDIT_BALANCE);
                }
                balanceBefore = userCredit.getAvailableBalance();
            }

            userCredit.subtractBalance(amount);
            balanceAfter = userCredit.getAvailableBalance();

            try {
                updateResult = userCreditMapper.updateById(userCredit);
            } catch (Exception e) {
                updateResult = 0;
            }

            if (updateResult <= 0) {
                retryCount++;
                if (retryCount < MAX_CONSUME_RETRIES) {
                    log.info("消费积分乐观锁冲突，准备重试，第{}次，userId={}, creditType={}",
                            retryCount, userId, scenario.getCreditTypeCode());
                    try {
                        Thread.sleep(50L * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (updateResult <= 0) {
            log.error("积分消费失败，重试{}次后仍失败，userId={}, creditType={}",
                    MAX_CONSUME_RETRIES, userId, scenario.getCreditTypeCode());
            throw new ServiceException(ErrorCode.CREDIT_SYSTEM_BESSY);
        }
        
        // 记录用户交易
        CreditTransaction transaction = new CreditTransaction();
        transaction.setUserSubject(userId);
        transaction.setCreditTypeCode(scenario.getCreditTypeCode());
        transaction.setTransactionType(CreditTransactionType.SPEND.getCode());
        transaction.setAmount(amount.negate()); // 消费为负数
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderId(orderId);
        transaction.setScenarioCode(scenarioCode);
        String costUnit = creditType.getUnitName() == null ? "" : creditType.getUnitName();
        String costTypeName = creditType.getTypeName() == null ? scenario.getCreditTypeCode() : creditType.getTypeName();
        String costText = amount.stripTrailingZeros().toPlainString();
        transaction.setDescription("消费 " + costText + " " + costUnit + "（类型=" + costTypeName + ", code=" + scenario.getCreditTypeCode() + "）"
                + "，使用场景: " + scenario.getScenarioName());
        
        try {
            int insertResult = creditTransactionMapper.insert(transaction);
            if (insertResult <= 0) {
                throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
            }
        } catch (DuplicateKeyException dup) {
            // 命中DB层幂等唯一键，视为幂等成功
            log.info("积分消费交易幂等唯一键冲突，视为成功，userId={}, orderId={}, scenario={}", userId, orderId, scenarioCode);
        }
        
        log.info("积分消费成功，用户ID: {}, 场景: {}, 消费积分数: {}, 余额: {} -> {}", 
                userId, scenarioCode, amount, balanceBefore, balanceAfter);

        // 发送消息通知（不影响主流程）
        try {
            String typeName = creditType.getTypeName() == null ? scenario.getCreditTypeCode() : creditType.getTypeName();
            String unitName = creditType.getUnitName() == null ? "" : creditType.getUnitName();
            String scenarioName = scenario.getScenarioName();
            notificationFacade.notifyCreditSpent(userId, scenarioName, typeName, unitName, amount, orderId, balanceBefore, balanceAfter);
        } catch (Exception e) {
            log.warn("发送积分消费消息失败，不影响业务，userId={}, scenarioCode={}, error={}", userId, scenarioCode, e.getMessage());
        }
        
        return userCredit;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit rewardCredits(String rewardType, BigDecimal amount) {
        Long userId = StpUtil.getLoginIdAsLong();
        return rewardCredits(userId, rewardType, amount, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit rewardCredits(Long targetUserId, String rewardType, BigDecimal amount) {
        return rewardCredits(targetUserId, rewardType, amount, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit rewardCredits(String rewardType, BigDecimal amount, Long relatedUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return rewardCredits(userId, rewardType, amount, relatedUserId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit rewardCredits(Long targetUserId, String rewardType, BigDecimal amount, Long relatedUserId) {
        return rewardCreditsWithRetry(targetUserId, rewardType, amount, relatedUserId, 3); // 最多重试3次
    }
    
    /**
     * 带重试机制的积分奖励方法
     */
    private UserCredit rewardCreditsWithRetry(Long targetUserId, String rewardType, BigDecimal amount, Long relatedUserId, int maxRetries) {
        log.info("奖励积分，目标用户ID: {}, 奖励类型: {}, 积分数: {}, 关联用户ID: {}, 最大重试次数: {}", 
                targetUserId, rewardType, amount, relatedUserId, maxRetries);
        
        // 参数验证增强
        if (targetUserId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "目标用户ID不能为空");
        }
        if (!StringUtils.hasText(rewardType) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "奖励类型不能为空，积分数必须大于0");
        }
        
        // 获取奖励场景信息
        CreditUsageScenario scenario = creditScenarioMapper.selectByScenarioCode(rewardType);
        if (scenario == null || !scenario.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_DISABLED, "奖励场景不存在或已禁用");
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED);
        }
        
        // 验证场景配置的合理性（奖励场景应该是负数或0）
        if (scenario.getCostPerUse().compareTo(BigDecimal.ZERO) > 0) {
            throw new ServiceException(ErrorCode.BUSINESS_RULE_VIOLATION, "奖励场景的每次使用积分应该小于等于0");
        }
        
        // 获取目标用户积分账户
        UserCredit userCredit = getUserCreditByUserIdAndType(targetUserId, scenario.getCreditTypeCode());
        if (userCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }
        
        // 增加积分（使用乐观锁）
        BigDecimal balanceBefore = userCredit.getAvailableBalance();
        userCredit.addBalance(amount);
        BigDecimal balanceAfter = userCredit.getAvailableBalance();
        
        // 使用乐观锁更新积分账户，支持重试（MyBatis-Plus会自动处理版本号）
        int updateResult = 0;
        int retryCount = 0;
        
        while (updateResult <= 0 && retryCount < maxRetries) {
            try {
                // 每次重试前，重新获取最新数据以确保版本号正确
                if (retryCount > 0) {
                    UserCredit latestUserCredit = getUserCreditByUserIdAndType(targetUserId, scenario.getCreditTypeCode());
                    if (latestUserCredit != null) {
                        // 重新计算余额
                        BigDecimal newBalanceBefore = latestUserCredit.getAvailableBalance();
                        latestUserCredit.addBalance(amount);
                        BigDecimal newBalanceAfter = latestUserCredit.getAvailableBalance();
                        
                        userCredit = latestUserCredit;
                        balanceBefore = newBalanceBefore;
                        balanceAfter = newBalanceAfter;
                        
                        // 让MyBatis-Plus自动处理版本号，不要手动设置
                    }
                }
                
                updateResult = userCreditMapper.updateById(userCredit);
                if (updateResult > 0) {
                    break; // 更新成功，跳出循环
                }
                
                // 更新失败，记录重试
                retryCount++;
                if (retryCount < maxRetries) {
                    log.info("乐观锁版本冲突，重新获取数据重试，重试次数: {}, 用户ID: {}, 积分类型: {}", 
                            retryCount, targetUserId, scenario.getCreditTypeCode());
                    
                    // 短暂等待后重试
                    try {
                        Thread.sleep(50 * retryCount); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("积分更新异常，重试次数: {}, 用户ID: {}, 积分类型: {}, 异常: {}", 
                        retryCount + 1, targetUserId, scenario.getCreditTypeCode(), e.getMessage());
                retryCount++;
            }
        }
        
        if (updateResult <= 0) {
            log.error("积分奖励失败，重试{}次后仍然失败，用户ID: {}, 积分类型: {}", 
                    maxRetries, targetUserId, scenario.getCreditTypeCode());
            throw new ServiceException(ErrorCode.CREDIT_SYSTEM_BESSY);
        }
        
        // 记录交易
        CreditTransaction transaction = new CreditTransaction();
        transaction.setUserId(targetUserId);
        // 设置用户交易主体
        transaction.setUserSubject(targetUserId);
        transaction.setCreditTypeCode(scenario.getCreditTypeCode());
        transaction.setTransactionType(CreditTransactionType.EARN.getCode()); // 奖励积分统一使用EARN类型
        transaction.setAmount(amount); // 奖励为正数
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedUserId(relatedUserId); // 记录关联用户ID（如推荐人、邀请人等）
        transaction.setScenarioCode(rewardType); // 具体的业务场景
        
        // 根据是否有关联用户调整描述信息
        String description = "奖励类型: " + scenario.getScenarioName();
        if (relatedUserId != null) {
            if (CreditScenarioCode.USER_REFERRAL.getCode().equals(rewardType)) {
                description += "，推荐新用户奖励";
            } else {
                description += "，关联用户ID: " + relatedUserId;
            }
        }
        transaction.setDescription(description);
        
        int insertResult = creditTransactionMapper.insert(transaction);
        if (insertResult <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }
        
        if (relatedUserId != null) {
            log.info("积分奖励成功，目标用户ID: {}, 奖励类型: {}, 奖励积分数: {}, 关联用户ID: {}, 余额: {} -> {}", 
                    targetUserId, rewardType, amount, relatedUserId, balanceBefore, balanceAfter);
        } else {
            log.info("积分奖励成功，目标用户ID: {}, 奖励类型: {}, 奖励积分数: {}, 余额: {} -> {}", 
                    targetUserId, rewardType, amount, balanceBefore, balanceAfter);
        }

        // 发送站内消息通知（不影响主流程）
        try {
            String typeName = creditType.getTypeName() == null ? scenario.getCreditTypeCode() : creditType.getTypeName();
            String unitName = creditType.getUnitName() == null ? "" : creditType.getUnitName();
            String scenarioName = scenario.getScenarioName();
            String relatedUserName = null;
            if (relatedUserId != null) {
                relatedUserName = getUserDisplayName(relatedUserId);
            }
            notificationFacade.notifyCreditRewarded(targetUserId, scenarioName, typeName, unitName, amount, relatedUserId, relatedUserName, balanceBefore, balanceAfter);
        } catch (Exception e) {
            log.warn("发送积分奖励消息失败，不影响业务，targetUserId={}, rewardType={}, error={}", targetUserId, rewardType, e.getMessage());
        }
        
        return userCredit;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit refundCredits(String orderId, BigDecimal amount) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("积分退款，用户ID: {}, 订单号: {}, 退款积分数: {}", userId, orderId, amount);
        
        // 参数验证增强
        if (!StringUtils.hasText(orderId) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "订单号不能为空，退款积分数必须大于0");
        }
        
        // 查找原消费交易记录
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditTransaction::getRelatedOrderId, orderId);
        List<CreditTransaction> transactions = creditTransactionMapper.selectList(queryWrapper);
        if (transactions.isEmpty()) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_NOT_FOUND, "未找到原消费交易记录");
        }
        
        // 找到对应的消费交易
        CreditTransaction consumeTransaction = null;
        for (CreditTransaction transaction : transactions) {
            if (CreditTransactionType.SPEND.getCode().equals(transaction.getTransactionType()) && 
                userId.equals(transaction.getUserId())) {
                consumeTransaction = transaction;
                break;
            }
        }
        
        if (consumeTransaction == null) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_NOT_FOUND, "未找到对应的消费交易记录");
        }
        
        // 验证退款金额不能超过原消费金额
        BigDecimal originalAmount = consumeTransaction.getAmount().abs();
        if (amount.compareTo(originalAmount) > 0) {
            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "退款金额不能超过原消费金额");
        }
        
        // 获取用户积分账户
        UserCredit userCredit = getUserCreditByUserIdAndType(userId, consumeTransaction.getCreditTypeCode());
        if (userCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }
        
        // 增加积分（退款，使用乐观锁）
        BigDecimal balanceBefore = userCredit.getAvailableBalance();
        userCredit.addBalance(amount);
        BigDecimal balanceAfter = userCredit.getAvailableBalance();
        
        // 使用乐观锁更新积分账户（MyBatis-Plus会自动处理版本号）
        int updateResult = userCreditMapper.updateById(userCredit);
        if (updateResult <= 0) {
            // 乐观锁版本冲突，重新获取最新数据重试
            log.warn("乐观锁版本冲突，用户ID: {}, 积分类型: {}, 当前版本: {}", 
                    userId, consumeTransaction.getCreditTypeCode(), userCredit.getVersion());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "积分退款失败，请重试");
        }
        
        // 记录退款交易
        CreditTransaction refundTransaction = new CreditTransaction();
        refundTransaction.setUserId(userId);
        // 设置用户交易主体
        refundTransaction.setUserSubject(userId);
        refundTransaction.setCreditTypeCode(consumeTransaction.getCreditTypeCode());
        refundTransaction.setTransactionType(CreditTransactionType.REFUND.getCode());
        refundTransaction.setAmount(amount); // 退款为正数
        refundTransaction.setBalanceBefore(balanceBefore);
        refundTransaction.setBalanceAfter(balanceAfter);
        refundTransaction.setRelatedOrderId(orderId);
        refundTransaction.setScenarioCode(consumeTransaction.getScenarioCode()); // 继承原消费交易的场景
        refundTransaction.setDescription("订单退款: " + orderId);
        
        int insertResult = creditTransactionMapper.insert(refundTransaction);
        if (insertResult <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }
        
        log.info("积分退款成功，用户ID: {}, 订单号: {}, 退款积分数: {}, 余额: {} -> {}", 
                userId, orderId, amount, balanceBefore, balanceAfter);

        // 发送站内消息通知（不影响主流程）
        try {
            String typeName = consumeTransaction.getCreditTypeCode();
            // 查询类型名称与单位
            CreditType creditType2 = creditTypeMapper.selectOne(new LambdaQueryWrapper<CreditType>()
                    .eq(CreditType::getTypeCode, consumeTransaction.getCreditTypeCode()));
            String unitName = creditType2 == null || creditType2.getUnitName() == null ? "" : creditType2.getUnitName();
            String typeNameResolved = creditType2 == null || creditType2.getTypeName() == null ? typeName : creditType2.getTypeName();
            notificationFacade.notifyCreditRefunded(userId, orderId, typeNameResolved, unitName, amount, balanceBefore, balanceAfter);
        } catch (Exception e) {
            log.warn("发送积分退款消息失败，不影响业务，userId={}, orderId={}, error={}", userId, orderId, e.getMessage());
        }
        
        return userCredit;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserCredit> initializeUserCredits(Long userId) {
        log.info("初始化用户积分账户，用户ID: {}", userId);
        
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户ID不能为空");
        }
        
        // 获取所有启用的积分类型
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getStatus, CreditType.STATUS_ENABLED);
        queryWrapper.orderByAsc(CreditType::getSortOrder);
        List<CreditType> creditTypes = creditTypeMapper.selectList(queryWrapper);
        if (creditTypes.isEmpty()) {
            log.warn("没有启用的积分类型，无法初始化用户积分账户");
            return java.util.Collections.emptyList();
        }
        
        List<UserCredit> userCredits = new java.util.ArrayList<>();
        
        for (CreditType creditType : creditTypes) {
            // 检查是否已存在积分账户
            if (hasUserCreditAccount(userId, creditType.getTypeCode())) {
                log.debug("用户积分账户已存在，跳过初始化，用户ID: {}, 积分类型: {}", userId, creditType.getTypeCode());
                continue;
            }
            
            // 创建积分账户
            UserCredit userCredit = new UserCredit();
            userCredit.setUserId(userId);
            userCredit.setCreditTypeCode(creditType.getTypeCode());
            userCredit.setBalance(BigDecimal.ZERO);
            userCredit.setTotalEarned(BigDecimal.ZERO);
            userCredit.setTotalConsumed(BigDecimal.ZERO);
            userCredit.setVersion(1); // 显式设置初始版本号为1
            
            int insertResult = userCreditMapper.insert(userCredit);
            if (insertResult > 0) {
                userCredits.add(userCredit);
                log.debug("用户积分账户初始化成功，用户ID: {}, 积分类型: {}", userId, creditType.getTypeCode());
            } else {
                log.error("用户积分账户初始化失败，用户ID: {}, 积分类型: {}", userId, creditType.getTypeCode());
            }
        }
        
        log.info("用户积分账户初始化完成，用户ID: {}, 成功初始化账户数: {}", userId, userCredits.size());
        
        return userCredits;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferCredits(Long fromUserId, Long toUserId, String creditTypeCode, BigDecimal amount) {
        log.info("积分转账，转出用户ID: {}, 转入用户ID: {}, 积分类型: {}, 积分数: {}", 
                fromUserId, toUserId, creditTypeCode, amount);
        
        // 参数验证增强
        if (fromUserId == null || toUserId == null || !StringUtils.hasText(creditTypeCode) || 
            amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "用户ID不能为空，积分类型不能为空，积分数必须大于0");
        }
        
        // 不能向自己转账
        if (fromUserId.equals(toUserId)) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSFER_SAME_USER);
        }
        
        // 验证积分类型是否支持转账
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, creditTypeCode);
        CreditType creditType = creditTypeMapper.selectOne(queryWrapper);
        if (creditType == null || !creditType.isEnabled() || !creditType.isTransferable()) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSFER_NOT_ALLOWED);
        }
        
        // 获取转出用户积分账户
        UserCredit fromUserCredit = getUserCreditByUserIdAndType(fromUserId, creditTypeCode);
        if (fromUserCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }
        
        // 验证余额是否足够
        if (!fromUserCredit.hasSufficientBalance(amount)) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSFER_INSUFFICIENT_BALANCE);
        }
        
        // 获取转入用户积分账户
        UserCredit toUserCredit = getUserCreditByUserIdAndType(toUserId, creditTypeCode);
        if (toUserCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }
        
        // 执行转账（使用乐观锁）
        BigDecimal fromBalanceBefore = fromUserCredit.getAvailableBalance();
        fromUserCredit.subtractBalance(amount);
        BigDecimal fromBalanceAfter = fromUserCredit.getAvailableBalance();
        
        BigDecimal toBalanceBefore = toUserCredit.getAvailableBalance();
        toUserCredit.addBalance(amount);
        BigDecimal toBalanceAfter = toUserCredit.getAvailableBalance();
        
        // 更新转出用户积分账户（使用乐观锁，MyBatis-Plus会自动处理版本号）
        int fromUpdateResult = userCreditMapper.updateById(fromUserCredit);
        if (fromUpdateResult <= 0) {
            // 乐观锁版本冲突，重新获取最新数据重试
            log.warn("乐观锁版本冲突，转出用户ID: {}, 积分类型: {}, 当前版本: {}", 
                    fromUserId, creditTypeCode, fromUserCredit.getVersion());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "转出用户积分扣减失败，请重试");
        }
        
        // 更新转入用户积分账户（使用乐观锁）
        int toUpdateResult = userCreditMapper.updateById(toUserCredit);
        if (toUpdateResult <= 0) {
            // 乐观锁版本冲突，重新获取最新数据重试
            log.warn("乐观锁版本冲突，转入用户ID: {}, 积分类型: {}, 当前版本: {}", 
                    toUserId, creditTypeCode, toUserCredit.getVersion());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "转入用户积分增加失败，请重试");
        }
        
        // 查询双方信息与类型显示，完善描述
        // 保留旧查询以备扩展，如需更多字段可复用；名称展示统一由 getUserDisplayName 提供
        // User fromUser = userMapper.selectById(fromUserId);
        // User toUser = userMapper.selectById(toUserId);
        String fromName = getUserDisplayName(fromUserId);
        String toName = getUserDisplayName(toUserId);
        String typeName2 = creditType.getTypeName() == null ? creditTypeCode : creditType.getTypeName();
        String unitName2 = creditType.getUnitName() == null ? "" : creditType.getUnitName();
        String amountText2 = amount.stripTrailingZeros().toPlainString();

        // 记录转出交易
        CreditTransaction fromTransaction = new CreditTransaction();
        fromTransaction.setUserId(fromUserId);
        // 设置用户交易主体
        fromTransaction.setUserSubject(fromUserId);
        fromTransaction.setCreditTypeCode(creditTypeCode);
        fromTransaction.setTransactionType(CreditTransactionType.TRANSFER.getCode());
        fromTransaction.setAmount(amount.negate()); // 转出为负数
        fromTransaction.setBalanceBefore(fromBalanceBefore);
        fromTransaction.setBalanceAfter(fromBalanceAfter);
        fromTransaction.setRelatedUserId(toUserId);
        fromTransaction.setScenarioCode(null); // 转账不需要特定场景编码
        fromTransaction.setDescription("向用户[id=" + toUserId + ",用户名=" + toName + "] 转出 "
                + amountText2 + " " + unitName2 + "（类型=" + typeName2 + ", code=" + creditTypeCode + "）" 
                + "；转出方[id=" + fromUserId + ",用户名=" + fromName + "]");
        
        int fromInsertResult = creditTransactionMapper.insert(fromTransaction);
        if (fromInsertResult <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }
        
        // 记录转入交易
        CreditTransaction toTransaction = new CreditTransaction();
        toTransaction.setUserId(toUserId);
        // 设置用户交易主体
        toTransaction.setUserSubject(toUserId);
        toTransaction.setCreditTypeCode(creditTypeCode);
        toTransaction.setTransactionType(CreditTransactionType.TRANSFER.getCode());
        toTransaction.setAmount(amount); // 转入为正数
        toTransaction.setBalanceBefore(toBalanceBefore);
        toTransaction.setBalanceAfter(toBalanceAfter);
        toTransaction.setRelatedUserId(fromUserId);
        toTransaction.setScenarioCode(null); // 转账不需要特定场景编码
        toTransaction.setDescription("收到来自用户[id=" + fromUserId + ",用户名=" + fromName + "] 的转入 "
                + amountText2 + " " + unitName2 + "（类型=" + typeName2 + ", code=" + creditTypeCode + "）" 
                + "；接收方[id=" + toUserId + ",用户名=" + toName + "]");
        
        int toInsertResult = creditTransactionMapper.insert(toTransaction);
        if (toInsertResult <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }
        
        // 建立转账双方的关联关系
        fromTransaction.setRelatedTransactionId(toTransaction.getId());
        toTransaction.setRelatedTransactionId(fromTransaction.getId());
        
        // 更新关联关系
        creditTransactionMapper.updateById(fromTransaction);
        creditTransactionMapper.updateById(toTransaction);
        
        log.info("积分转账成功，转出用户ID: {}, 转入用户ID: {}, 积分类型: {}, 积分数: {}, 转出余额: {} -> {}, 转入余额: {} -> {}", 
                fromUserId, toUserId, creditTypeCode, amount, fromBalanceBefore, fromBalanceAfter, toBalanceBefore, toBalanceAfter);
        
        // 发送站内消息通知（不影响主流程）
        try {
            notificationFacade.notifyCreditTransferOut(fromUserId, fromName == null ? toName : toName, typeName2, unitName2, amount, fromBalanceBefore, fromBalanceAfter);
            notificationFacade.notifyCreditTransferIn(toUserId, fromName, typeName2, unitName2, amount, toBalanceBefore, toBalanceAfter);
        } catch (Exception e) {
            log.warn("发送积分转账消息失败，不影响业务，fromUserId={}, toUserId={}, error={}", fromUserId, toUserId, e.getMessage());
        }

        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit allocateCredits(Long targetUserId, String creditTypeCode, BigDecimal amount, String description) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("管理员分配积分，目标用户ID: {}, 积分类型: {}, 积分数: {}, 描述: {}", 
                targetUserId, creditTypeCode, amount, description);
        
        // 参数验证增强
        if (targetUserId == null || !StringUtils.hasText(creditTypeCode) || 
            amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "目标用户ID不能为空，积分类型不能为空，积分数必须大于0");
        }
        // 禁止给自己分配
        if (userId != null && userId.equals(targetUserId)) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSFER_SAME_USER, "不能给自己分配积分");
        }
        
        // 验证当前用户是否有分配积分的权限
        if (!hasCreditAllocationPermission(userId)) {
            throw new ServiceException(ErrorCode.PERMISSION_DENIED, "没有权限分配积分");
        }
        
        // 验证积分类型是否存在
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, creditTypeCode);
        CreditType creditType = creditTypeMapper.selectOne(queryWrapper);
        if (creditType == null || !creditType.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED, "积分类型不存在或已禁用");
        }
        
        // 获取分配方（当前管理员）积分账户并校验余额
        UserCredit allocatorCredit = getUserCreditByUserIdAndType(userId, creditTypeCode);
        if (allocatorCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND, "分配方积分账户不存在");
        }
        if (!allocatorCredit.hasSufficientBalance(amount)) {
            throw new ServiceException(ErrorCode.INSUFFICIENT_CREDIT_BALANCE, "分配方积分余额不足");
        }

        // 获取目标用户积分账户
        UserCredit targetCredit = getUserCreditByUserIdAndType(targetUserId, creditTypeCode);
        if (targetCredit == null) {
            throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
        }

        // 计算并更新余额（使用乐观锁）
        BigDecimal allocatorBefore = allocatorCredit.getAvailableBalance();
        allocatorCredit.subtractBalance(amount);
        BigDecimal allocatorAfter = allocatorCredit.getAvailableBalance();

        BigDecimal targetBefore = targetCredit.getAvailableBalance();
        targetCredit.addBalance(amount);
        BigDecimal targetAfter = targetCredit.getAvailableBalance();

        int allocUpdate = userCreditMapper.updateById(allocatorCredit);
        if (allocUpdate <= 0) {
            log.warn("乐观锁版本冲突（分配方），管理员ID: {}, 积分类型: {}, 当前版本: {}", userId, creditTypeCode, allocatorCredit.getVersion());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "分配失败：分配方扣减失败，请重试");
        }

        int targetUpdate = userCreditMapper.updateById(targetCredit);
        if (targetUpdate <= 0) {
            log.warn("乐观锁版本冲突（接收方），目标用户ID: {}, 积分类型: {}, 当前版本: {}", targetUserId, creditTypeCode, targetCredit.getVersion());
            throw new ServiceException(ErrorCode.TRANSACTION_FAILED, "分配失败：接收方增加失败，请重试");
        }

        // 查询分配者与接收者信息，构建更详细说明
        // User adminUser = userMapper.selectById(userId);
        // User targetUser = userMapper.selectById(targetUserId);
        String adminName = getUserDisplayName(userId);
        String targetName = getUserDisplayName(targetUserId);
        String unitName = creditType.getUnitName() == null ? "" : creditType.getUnitName();
        String typeName = creditType.getTypeName() == null ? creditTypeCode : creditType.getTypeName();
        String remark = StringUtils.hasText(description) ? description : "无";
        String amountText = amount.stripTrailingZeros().toPlainString();

        // 记录交易：分配方（负数）
        CreditTransaction allocatorTx = new CreditTransaction();
        allocatorTx.setUserId(userId);
        // 设置用户交易主体
        allocatorTx.setUserSubject(userId);
        allocatorTx.setCreditTypeCode(creditTypeCode);
        allocatorTx.setTransactionType(CreditTransactionType.ADMIN.getCode());
        allocatorTx.setAmount(amount.negate()); // 分配方扣减
        allocatorTx.setBalanceBefore(allocatorBefore);
        allocatorTx.setBalanceAfter(allocatorAfter);
        allocatorTx.setRelatedUserId(targetUserId);
        allocatorTx.setScenarioCode(CreditScenarioCode.ADMIN_GRANT.getCode());
        allocatorTx.setDescription("向用户[" + targetName + "] 分配 "
                + amountText + " " + unitName + "（" + typeName + "）" 
                + "；分配方[" + adminName + "]；备注：" + remark);

        int allocInsert = creditTransactionMapper.insert(allocatorTx);
        if (allocInsert <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }

        // 记录交易：接收方（正数）
        CreditTransaction targetTx = new CreditTransaction();
        targetTx.setUserId(targetUserId);
        // 设置用户交易主体
        targetTx.setUserSubject(targetUserId);
        targetTx.setCreditTypeCode(creditTypeCode);
        targetTx.setTransactionType(CreditTransactionType.ADMIN.getCode());
        targetTx.setAmount(amount); // 接收方增加
        targetTx.setBalanceBefore(targetBefore);
        targetTx.setBalanceAfter(targetAfter);
        targetTx.setRelatedUserId(userId);
        targetTx.setScenarioCode(CreditScenarioCode.ADMIN_GRANT.getCode());
        targetTx.setDescription("收到来自管理员[" + adminName + "] 的分配 "
                + amountText + " " + unitName + "（" + typeName + "）" 
                + "；接收方[" + targetName + "]；备注：" + remark);

        int targetInsert = creditTransactionMapper.insert(targetTx);
        if (targetInsert <= 0) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_FAILED);
        }

        // 建立关联关系
        allocatorTx.setRelatedTransactionId(targetTx.getId());
        targetTx.setRelatedTransactionId(allocatorTx.getId());
        creditTransactionMapper.updateById(allocatorTx);
        creditTransactionMapper.updateById(targetTx);

        log.info("积分分配成功，管理员ID: {}, 目标用户ID: {}, 类型: {}, 数量: {}, 分配方余额: {} -> {}，接收方余额: {} -> {}", 
                userId, targetUserId, creditTypeCode, amount, allocatorBefore, allocatorAfter, targetBefore, targetAfter);

        // 发送站内消息通知（不影响主流程）
        try {
            notificationFacade.notifyCreditAllocatedOut(userId, targetName, typeName, unitName, amount, remark, allocatorBefore, allocatorAfter);
            notificationFacade.notifyCreditAllocatedIn(targetUserId, adminName, typeName, unitName, amount, remark, targetBefore, targetAfter);
        } catch (Exception e) {
            log.warn("发送积分分配消息失败，不影响业务，adminId={}, targetUserId={}, error={}", userId, targetUserId, e.getMessage());
        }

        return targetCredit;
    }
    
    @Override
    public List<UserCredit> getUserCreditBalanceList(String creditTypeCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("获取用户积分余额列表，用户ID: {}, 积分类型: {}", userId, creditTypeCode);
        
        if (StringUtils.hasText(creditTypeCode)) {
            // 获取指定类型的积分账户
            UserCredit userCredit = getUserCreditByUserIdAndType(userId, creditTypeCode);
            return userCredit != null ? java.util.Arrays.asList(userCredit) : java.util.Collections.emptyList();
        } else {
            // 获取所有类型的积分账户
            return userCreditMapper.selectByUserId(userId);
        }
    }

    @Override
    public List<UserCredit> getUserCreditBalanceListByUserId(Long targetUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        String role = getUserRole(currentUserId);
        if (targetUserId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "目标用户ID不能为空");
        }
        if (!canViewUser(currentUserId, role, targetUserId)) {
            throw new ServiceException(ErrorCode.PERMISSION_DENIED, "权限不足：无法查看该用户积分");
        }
        return userCreditMapper.selectByUserId(targetUserId);
    }
    
    @Override
    public BigDecimal getUserCreditBalance(String creditTypeCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("获取用户指定积分类型余额，用户ID: {}, 积分类型: {}", userId, creditTypeCode);
        
        if (!StringUtils.hasText(creditTypeCode)) {
            return BigDecimal.ZERO;
        }
        
        UserCredit userCredit = getUserCreditByUserIdAndType(userId, creditTypeCode);
        return userCredit != null ? userCredit.getAvailableBalance() : BigDecimal.ZERO;
    }
    
    @Override
    public boolean hasCreditAccount(String creditTypeCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!StringUtils.hasText(creditTypeCode)) {
            return false;
        }
        
        return hasUserCreditAccount(userId, creditTypeCode);
    }
    
    @Override
    public Object getConsumptionRule(String scenarioCode) {
        log.debug("获取积分消费规则，场景编码: {}", scenarioCode);
        
        if (!StringUtils.hasText(scenarioCode)) {
            return null;
        }
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            return null;
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            return null;
        }
        
        // 只返回消费场景（正数积分数）
        if (scenario.getCostPerUse().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("场景配置错误：消费场景的每次使用积分应该大于0，场景编码: {}, 当前值: {}", 
                    scenarioCode, scenario.getCostPerUse());
            return null;
        }
        
        return scenario;
    }
    
    @Override
    public Object getRewardRule(String rewardType) {
        log.debug("获取积分奖励规则，奖励类型: {}", rewardType);
        
        if (!StringUtils.hasText(rewardType)) {
            return null;
        }
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, rewardType);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            return null;
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            return null;
        }
        
        // 只返回奖励场景（负数积分数）
        if (scenario.getCostPerUse().compareTo(BigDecimal.ZERO) >= 0) {
            log.warn("场景配置错误：奖励场景的每次使用积分应该小于0，场景编码: {}, 当前值: {}", 
                    rewardType, scenario.getCostPerUse());
            return null;
        }
        
        return scenario;
    }
    
    @Override
    public boolean isValidCreditType(String creditTypeCode) {
        if (!StringUtils.hasText(creditTypeCode)) {
            return false;
        }
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, creditTypeCode);
        CreditType creditType = creditTypeMapper.selectOne(queryWrapper);
        return creditType != null && creditType.isEnabled();
    }
    
    @Override
    public boolean isValidScenario(String scenarioCode) {
        if (!StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            return false;
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        return creditType != null && creditType.isEnabled();
    }
    
    @Override
    public boolean canUserUseScenario(String scenarioCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("检查用户是否可以使用指定场景，用户ID: {}, 场景编码: {}", userId, scenarioCode);
        
        if (!StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        // 获取场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            return false;
        }
        
        // 校验积分类型启用状态
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            return false;
        }
        
        // 检查用户角色权限
        if (StringUtils.hasText(scenario.getUserRoles())) {
            String[] allowedRoles = scenario.getUserRoles().split(",");
            String userRole = getUserRole(userId);
            if (userRole == null) {
                return false;
            }
            for (String role : allowedRoles) {
                if (role != null && role.trim().equalsIgnoreCase(userRole)) {
                    return true;
                }
            }
            return false;
        }
        
        // 如果没有角色限制，默认允许所有用户使用
        return true;
    }
    
    @Override
    public boolean isDailyLimitExceeded(String scenarioCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("检查用户是否超过每日使用限制，用户ID: {}, 场景编码: {}", userId, scenarioCode);
        
        if (!StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        // 获取场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled() || scenario.getDailyLimit() == null) {
            return false; // 没有限制或场景不存在
        }
        
        // 查询今日使用次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        LambdaQueryWrapper<CreditTransaction> transactionQuery = new LambdaQueryWrapper<>();
        transactionQuery.eq(CreditTransaction::getUserId, userId);
        transactionQuery.eq(CreditTransaction::getScenarioCode, scenarioCode);
        transactionQuery.eq(CreditTransaction::getTransactionType, CreditTransactionType.SPEND.getCode());
        transactionQuery.ge(CreditTransaction::getCreateTime, todayStart);
        transactionQuery.lt(CreditTransaction::getCreateTime, todayEnd);
        transactionQuery.eq(CreditTransaction::getIsDeleted, 0);
        
        Long todayUsageCount = creditTransactionMapper.selectCount(transactionQuery);
        
        boolean exceeded = todayUsageCount >= scenario.getDailyLimit();
        if (exceeded) {
            log.warn("用户每日使用限制已超，用户ID: {}, 场景: {}, 今日使用: {}, 限制: {}", 
                    userId, scenarioCode, todayUsageCount, scenario.getDailyLimit());
        }
        
        return exceeded;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredit applyScenario(String scenarioCode, String relatedOrderId, Long relatedUserId, String description) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("按场景执行积分操作，用户ID: {}, 场景: {}, 订单号: {}", userId, scenarioCode, relatedOrderId);

        if (!StringUtils.hasText(scenarioCode)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景编码不能为空");
        }

        // 读取场景配置并校验启用、类型状态
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_NOT_FOUND, "使用场景不存在");
        }
        if (!scenario.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_DISABLED, "使用场景已禁用");
        }

        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, scenario.getCreditTypeCode());
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null || !creditType.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED, "积分类型不存在或已禁用");
        }

        BigDecimal costPerUse = scenario.getCostPerUse();
        if (costPerUse == null) {
            throw new ServiceException(ErrorCode.BUSINESS_RULE_VIOLATION, "场景配置不完整：缺少每次使用积分");
        }

        // 0 积分：返回账户快照
        if (costPerUse.compareTo(BigDecimal.ZERO) == 0) {
            UserCredit userCredit = getUserCreditByUserIdAndType(userId, scenario.getCreditTypeCode());
            if (userCredit == null) {
                throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
            }
            return userCredit;
        }

        // 奖励：负数 -> 发放 abs(cost)
        if (costPerUse.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal rewardAmount = costPerUse.abs();
            return rewardCredits(userId, scenarioCode, rewardAmount, relatedUserId);
        }

        // 消费：正数 -> 前置扣费；支持订单号防重复
        BigDecimal consumeAmount = costPerUse;

        // 幂等：若传了订单号，检查是否已有消费交易
        if (StringUtils.hasText(relatedOrderId)) {
            LambdaQueryWrapper<CreditTransaction> txQuery = new LambdaQueryWrapper<>();
            txQuery.eq(CreditTransaction::getUserId, userId)
                   .eq(CreditTransaction::getRelatedOrderId, relatedOrderId)
                   .eq(CreditTransaction::getTransactionType, CreditTransactionType.SPEND.getCode());
            Long exist = creditTransactionMapper.selectCount(txQuery);
            if (exist != null && exist > 0) {
                // 已扣过费，直接返回账户快照
                UserCredit userCredit = getUserCreditByUserIdAndType(userId, scenario.getCreditTypeCode());
                if (userCredit == null) {
                    throw new ServiceException(ErrorCode.USER_CREDIT_ACCOUNT_NOT_FOUND);
                }
                log.info("积分消费幂等检查通过，用户ID: {}, 订单号: {}, 场景: {}", userId, relatedOrderId, scenarioCode);
                return userCredit;
            }
        }

        // 权限与每日限次
        if (!canUserUseScenario(scenarioCode)) {
            throw new ServiceException(ErrorCode.CREDIT_USER_ROLE_NOT_ALLOWED);
        }
        if (isDailyLimitExceeded(scenarioCode)) {
            throw new ServiceException(ErrorCode.CREDIT_DAILY_LIMIT_EXCEEDED);
        }

        // 简化：统一走用户账户扣减
        return consumeCredits(scenarioCode, consumeAmount, relatedOrderId);
    }

    @Override
    public IPage<UserCreditsSummaryResponse> getUserCreditsSummaryPage(Page<User> page, String keyword) {
        log.info("分页查询用户积分汇总，页码: {}, 大小: {}, 关键词: {}", page.getCurrent(), page.getSize(), keyword);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        String role = getUserRole(currentUserId);

        // 分页查询用户（仅启用用户，按数据域过滤）
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            userQuery.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().eq(User::getId, tryParseLong(keyword))
            );
        }
        // 数据域：USER仅自己；ADMIN仅自己+子账号；SUPER_ADMIN所有
        if (!User.ROLE_SUPER_ADMIN.equals(role)) {
            java.util.Set<Long> visibleUserIds = new java.util.HashSet<>();
            visibleUserIds.add(currentUserId);
            if (User.ROLE_ADMIN.equals(role)) {
                visibleUserIds.addAll(getAllSubUserIds(currentUserId));
            }
            if (visibleUserIds.isEmpty()) {
                // 确保不会查到任何记录
                userQuery.eq(User::getId, -1L);
            } else {
                userQuery.in(User::getId, visibleUserIds);
            }
        }
        IPage<User> userPage = userMapper.selectPage(page, userQuery);

        // 组装响应
        List<User> users = userPage.getRecords();
        List<UserCreditsSummaryResponse> records = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                UserCreditsSummaryResponse summary = new UserCreditsSummaryResponse();
                summary.setUserId(user.getId());
                summary.setUsername(user.getUsername());
                summary.setNickname(user.getNickname());
                summary.setRole(user.getRole());

                // 查询该用户的积分账户列表
                List<UserCredit> userCredits = userCreditMapper.selectByUserId(user.getId());
                List<CreditBalanceResponse.CreditAccountInfo> accounts = new ArrayList<>();
                BigDecimal totalConsumedSum = BigDecimal.ZERO;
                for (UserCredit uc : userCredits) {
                    CreditBalanceResponse.CreditAccountInfo info = new CreditBalanceResponse.CreditAccountInfo();
                    info.setCreditTypeCode(uc.getCreditTypeCode());
                    info.setBalance(uc.getAvailableBalance());
                    info.setTotalEarned(uc.getTotalEarnedAmount());
                    info.setTotalConsumed(uc.getTotalConsumedAmount());
                    totalConsumedSum = totalConsumedSum.add(uc.getTotalConsumedAmount());

                    // 附加类型显示信息
                    CreditType creditType = creditTypeMapper.selectOne(new LambdaQueryWrapper<CreditType>()
                            .eq(CreditType::getTypeCode, uc.getCreditTypeCode()));
                    if (creditType != null) {
                        info.setCreditTypeName(creditType.getTypeName());
                        info.setUnitName(creditType.getUnitName());
                        info.setIconUrl(creditType.getIconUrl());
                        info.setColorCode(creditType.getColorCode());
                        info.setDecimalPlaces(creditType.getDecimalPlaces());
                        info.setIsTransferable(creditType.isTransferable());
                    }
                    accounts.add(info);
                }
                summary.setAccounts(accounts);
                summary.setTotalConsumed(totalConsumedSum);
                records.add(summary);
            }
        }

        Page<UserCreditsSummaryResponse> result = new Page<>();
        result.setCurrent(userPage.getCurrent());
        result.setSize(userPage.getSize());
        result.setTotal(userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    private Long tryParseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            return -1L; // 不匹配任何ID
        }
    }
    
    /**
     * 递归/迭代获取当前管理员的所有子用户ID
     */
    private java.util.Set<Long> getAllSubUserIds(Long adminUserId) {
        java.util.Set<Long> result = new java.util.HashSet<>();
        java.util.Deque<Long> queue = new java.util.ArrayDeque<>();
        java.util.Set<Long> visited = new java.util.HashSet<>();
        queue.add(adminUserId);
        visited.add(adminUserId);
        int safety = 0;
        while (!queue.isEmpty() && safety < 10000) {
            Long parentId = queue.poll();
            safety++;
            List<User> children = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .eq(User::getParentUserId, parentId)
                    .eq(User::getIsDeleted, 0));
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (User child : children) {
                if (child == null || child.getId() == null) continue;
                if (visited.add(child.getId())) {
                    result.add(child.getId());
                    queue.add(child.getId());
                }
            }
        }
        return result;
    }

    /**
     * 获取当前用户角色（单主角色）
     */
    private String getUserRole(Long userId) {
        try {
            java.util.List<String> roles = cn.dev33.satoken.stp.StpUtil.getRoleList(userId);
            if (roles == null || roles.isEmpty()) {
                return User.ROLE_USER;
            }
            String role = roles.get(0);
            if (User.ROLE_SUPER_ADMIN.equalsIgnoreCase(role)) return User.ROLE_SUPER_ADMIN;
            if (User.ROLE_ADMIN.equalsIgnoreCase(role)) return User.ROLE_ADMIN;
            return User.ROLE_USER;
        } catch (Exception e) {
            return User.ROLE_USER;
        }
    }
    
    // 发送积分相关消息收敛至 NotificationFacade
    
    /**
     * 获取用户的展示名称：优先昵称，其次用户名；失败时返回字符串形式的ID。
     */
    private String getUserDisplayName(Long userId) {
        if (userId == null) {
            return "-";
        }
        try {
            User u = userMapper.selectById(userId);
            if (u == null) {
                return String.valueOf(userId);
            }
            String name = u.getNickname();
            if (!org.springframework.util.StringUtils.hasText(name)) {
                name = u.getUsername();
            }
            return org.springframework.util.StringUtils.hasText(name) ? name : String.valueOf(userId);
        } catch (Exception e) {
            return String.valueOf(userId);
        }
    }
    // ================ 私有辅助方法 ================
    
    /**
     * 根据用户ID和积分类型获取用户积分账户
     */
    private UserCredit getUserCreditByUserIdAndType(Long userId, String creditTypeCode) {
        LambdaQueryWrapper<UserCredit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCredit::getUserId, userId);
        queryWrapper.eq(UserCredit::getCreditTypeCode, creditTypeCode);
        return userCreditMapper.selectOne(queryWrapper);
    }
    
    /**
     * 检查用户是否拥有指定类型的积分账户
     */
    private boolean hasUserCreditAccount(Long userId, String creditTypeCode) {
        LambdaQueryWrapper<UserCredit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCredit::getUserId, userId);
        queryWrapper.eq(UserCredit::getCreditTypeCode, creditTypeCode);
        return userCreditMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查当前用户是否有权限进行积分分配
     */
    private boolean hasCreditAllocationPermission(Long userId) {
        // 只有管理员或超级管理员允许分配积分
        if (userId == null) {
            return false;
        }
        try {
            // 优先使用 Sa-Token 检查角色
            // 单一主角色模型：ADMIN 或 SUPER_ADMIN
            String role = cn.dev33.satoken.stp.StpUtil.getRoleList(userId).stream()
                    .findFirst()
                    .orElse(null);
            if (role == null) {
                return false;
            }
            return com.okbug.platform.entity.auth.User.ROLE_ADMIN.equals(role)
                    || com.okbug.platform.entity.auth.User.ROLE_SUPER_ADMIN.equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    // 重复的 getUserRole 方法已移除，统一使用前文实现

    /**
     * 是否可查看目标用户（数据域）：
     * - SUPER_ADMIN: 任意
     * - ADMIN: 自己 + 子账号
     * - USER: 仅自己
     */
    private boolean canViewUser(Long currentUserId, String role, Long targetUserId) {
        if (currentUserId == null || targetUserId == null) {
            return false;
        }
        if (User.ROLE_SUPER_ADMIN.equals(role)) {
            return true;
        }
        if (User.ROLE_ADMIN.equals(role)) {
            if (currentUserId.equals(targetUserId)) return true;
            return getAllSubUserIds(currentUserId).contains(targetUserId);
        }
        return currentUserId.equals(targetUserId);
    }
} 