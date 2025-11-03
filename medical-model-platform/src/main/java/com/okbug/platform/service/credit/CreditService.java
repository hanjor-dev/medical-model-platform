/**
 * 积分核心业务Service接口：定义积分系统的核心业务操作
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
 * @date 2025-01-15 11:00:00
 */
package com.okbug.platform.service.credit;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.credit.response.UserCreditsSummaryResponse;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.credit.UserCredit;

public interface CreditService {
    
    /**
     * 验证当前用户积分余额是否足够
     * 
     * @param scenarioCode 使用场景编码
     * @param amount 需要验证的积分数
     * @return 验证结果，true表示余额足够，false表示余额不足
     */
    boolean validateBalance(String scenarioCode, BigDecimal amount);
    
    /**
     * 消费积分（扣减当前用户积分）
     * 
     * @param scenarioCode 使用场景编码
     * @param amount 消费积分数
     * @param orderId 关联订单号（可选）
     * @return 消费后的用户积分账户
     */
    UserCredit consumeCredits(String scenarioCode, BigDecimal amount, String orderId);
    
    /**
     * 奖励积分（增加当前用户积分）
     * 
     * @param rewardType 奖励类型（如REGISTER、REFERRAL等）
     * @param amount 奖励积分数
     * @return 奖励后的用户积分账户
     */
    UserCredit rewardCredits(String rewardType, BigDecimal amount);
    
    /**
     * 奖励积分（增加指定用户积分）
     * 
     * @param targetUserId 目标用户ID
     * @param rewardType 奖励类型（如REGISTER、REFERRAL等）
     * @param amount 奖励积分数
     * @return 奖励后的用户积分账户
     */
    UserCredit rewardCredits(Long targetUserId, String rewardType, BigDecimal amount);
    
    /**
     * 奖励积分（增加当前用户积分，记录关联用户）
     * 
     * @param rewardType 奖励类型（如REGISTER、REFERRAL等）
     * @param amount 奖励积分数
     * @param relatedUserId 关联用户ID（推荐人、邀请人等）
     * @return 奖励后的用户积分账户
     */
    UserCredit rewardCredits(String rewardType, BigDecimal amount, Long relatedUserId);
    
    /**
     * 奖励积分（增加指定用户积分，记录关联用户）
     * 
     * @param targetUserId 目标用户ID
     * @param rewardType 奖励类型（如REGISTER、REFERRAL等）
     * @param amount 奖励积分数
     * @param relatedUserId 关联用户ID（推荐人、邀请人等）
     * @return 奖励后的用户积分账户
     */
    UserCredit rewardCredits(Long targetUserId, String rewardType, BigDecimal amount, Long relatedUserId);
    
    /**
     * 积分退款
     * 
     * @param orderId 关联订单号
     * @param amount 退款积分数
     * @return 退款后的用户积分账户
     */
    UserCredit refundCredits(String orderId, BigDecimal amount);
    
    /**
     * 初始化用户积分账户
     * 
     * @param userId 用户ID
     * @return 初始化后的积分账户列表
     */
    List<UserCredit> initializeUserCredits(Long userId);
    
    /**
     * 用户间积分转账
     * 
     * @param fromUserId 转出用户ID
     * @param toUserId 转入用户ID
     * @param creditTypeCode 积分类型编码
     * @param amount 转账积分数
     * @return 转账结果，true表示成功，false表示失败
     */
    boolean transferCredits(Long fromUserId, Long toUserId, String creditTypeCode, BigDecimal amount);
    
    /**
     * 管理员向用户分配积分
     * 
     * @param targetUserId 目标用户ID
     * @param creditTypeCode 积分类型编码
     * @param amount 分配积分数
     * @param description 分配描述
     * @return 分配后的用户积分账户
     */
    UserCredit allocateCredits(Long targetUserId, String creditTypeCode, BigDecimal amount, String description);
    
    /**
     * 获取当前用户积分余额列表
     * 
     * @param creditTypeCode 积分类型编码（可选，为null时返回所有类型）
     * @return 积分余额信息列表
     */
    List<UserCredit> getUserCreditBalanceList(String creditTypeCode);

    /**
     * 获取指定用户的积分余额列表（含数据域校验）
     *
     * @param userId 目标用户ID
     * @return 指定用户的积分余额信息列表
     */
    List<UserCredit> getUserCreditBalanceListByUserId(Long userId);
    
    /**
     * 获取当前用户指定积分类型的余额
     * 
     * @param creditTypeCode 积分类型编码
     * @return 积分余额，如果账户不存在返回0
     */
    BigDecimal getUserCreditBalance(String creditTypeCode);
    
    /**
     * 检查当前用户是否有指定类型的积分账户
     * 
     * @param creditTypeCode 积分类型编码
     * @return 是否存在积分账户
     */
    boolean hasCreditAccount(String creditTypeCode);
    
    /**
     * 获取积分消费规则
     * 
     * @param scenarioCode 使用场景编码
     * @return 消费规则信息
     */
    Object getConsumptionRule(String scenarioCode);
    
    /**
     * 获取积分奖励规则
     * 
     * @param rewardType 奖励类型
     * @return 奖励规则信息
     */
    Object getRewardRule(String rewardType);
    
    /**
     * 验证积分类型是否有效
     * 
     * @param creditTypeCode 积分类型编码
     * @return 是否有效
     */
    boolean isValidCreditType(String creditTypeCode);
    
    /**
     * 验证使用场景是否有效
     * 
     * @param scenarioCode 使用场景编码
     * @return 是否有效
     */
    boolean isValidScenario(String scenarioCode);
    
    /**
     * 检查当前用户是否可以使用指定场景
     * 
     * @param scenarioCode 使用场景编码
     * @return 是否可以使用
     */
    boolean canUserUseScenario(String scenarioCode);
    
    /**
     * 检查当前用户是否超过每日使用限制
     * 
     * @param scenarioCode 使用场景编码
     * @return 是否超过限制
     */
    boolean isDailyLimitExceeded(String scenarioCode);

    /**
     * 统一按场景执行积分操作
     *
     * 说明：
     * - 场景为消费(cost_per_use > 0)时，执行前置扣费；
     * - 场景为奖励(cost_per_use < 0)时，执行发放；
     * - 场景为0时，不做余额变更但返回当前账户快照；
     * - 建议消费场景传入 relatedOrderId 以支持幂等与后续退款。
     *
     * @param scenarioCode 使用场景编码
     * @param relatedOrderId 关联订单号（消费建议必传，用于幂等/退款）
     * @param relatedUserId 关联用户ID（如推荐人等，奖励时可用）
     * @param description 描述信息
     * @return 执行后的用户积分账户快照
     */
    UserCredit applyScenario(String scenarioCode, String relatedOrderId, Long relatedUserId, String description);

    /**
     * 分页查询用户 + 积分账户汇总（管理员）
     *
     * @param page 分页参数
     * @param keyword 用户关键词（用户名/昵称/邮箱/手机号/ID）
     * @return 用户积分汇总分页结果
     */
    IPage<UserCreditsSummaryResponse> getUserCreditsSummaryPage(Page<User> page, String keyword);
} 