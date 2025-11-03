/**
 * 积分交易记录实体类：对应数据库credit_transactions表
 * 
 * 功能描述：
 * 1. 记录所有积分收入和支出交易
 * 2. 支持多种交易类型和关联信息
 * 3. 记录交易前后的余额变化
 * 4. 支持软删除和审计字段
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:15:00
 */
package com.okbug.platform.entity.credit;

import com.baomidou.mybatisplus.annotation.*;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;
import com.okbug.platform.common.enums.credit.CreditTransactionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_transactions")
public class CreditTransaction {
    
    /**
     * 交易ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    // 已精简：仅保留用户维度交易，移除主体类型和团队相关字段
    
    /**
     * 积分类型编码
     */
    private String creditTypeCode;
    
    /**
     * 交易类型（EARN:获得积分 SPEND:消费积分 TRANSFER:积分转账 REFUND:积分退款 ADMIN:管理员操作）
     */
    private String transactionType;
    
    /**
     * 交易金额（正数为收入，负数为支出）
     */
    private BigDecimal amount;
    
    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;
    
    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;
    
    /**
     * 关联用户ID（转账、推荐等）
     */
    private Long relatedUserId;
    
    /**
     * 关联订单号
     */
    private String relatedOrderId;
    
    /**
     * 关联交易ID（用于转账等双向交易）
     */
    private Long relatedTransactionId;
    
    /**
     * 使用场景编码
     */
    private String scenarioCode;
    
    /**
     * 交易描述
     */
    private String description;
    
    /**
     * 逻辑删除标记（0:正常 1:删除）
     */
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // ================ 字段说明和使用指南 ================
    
    /**
     * transactionType 字段说明：
     * 
     * 表示交易的财务性质，可选值：
     * - EARN: 获得积分（注册奖励、推荐奖励、任务完成等）
     * - SPEND: 消费积分（AI计算、阅片、文件上传等）
     * - TRANSFER: 积分转账（用户间转移）
     * - REFUND: 积分退款（退回已消费的积分）
     * - ADMIN: 管理员操作（手动调整积分）
     * 
     * 使用方式：
     * transaction.setTransactionType(CreditTransactionType.EARN.getCode());
     */
    
    /**
     * scenarioCode 字段说明：
     * 
     * 表示具体的业务场景，对应CreditScenarioCode枚举值：
     * 
     * 奖励场景：
     * - USER_REGISTER: 用户注册奖励
     * - USER_REFERRAL: 推荐奖励
     * - FIRST_LOGIN: 首次登录奖励
     * - DAILY_CHECKIN: 每日签到奖励
     * - TASK_COMPLETE: 任务完成奖励
     * 
     * 消费场景：
     * - AI_COMPUTE: AI计算
     * - READING: 用户阅片
     * - FILE_UPLOAD: 文件上传
     * - MODEL_VIEW: 模型查看
     * - MODEL_DOWNLOAD: 模型下载
     * 
     * 特殊场景：
     * - ADMIN_GRANT: 管理员发放
     * - VIP_UPGRADE: VIP升级
     * - ACTIVITY_REWARD: 活动奖励
     * 
     * 使用方式：
     * transaction.setScenarioCode(CreditScenarioCode.USER_REGISTER.getCode());
     */
    
    // ================ 便利方法 ================
    
    /**
     * 判断是否为收入交易（获得积分）
     * 
     * @return true:收入交易 false:非收入交易
     */
    public boolean isIncomeTransaction() {
        return CreditTransactionType.EARN.getCode().equals(this.transactionType);
    }
    
    /**
     * 判断是否为支出交易（消费积分）
     * 
     * @return true:支出交易 false:非支出交易
     */
    public boolean isExpenseTransaction() {
        return CreditTransactionType.SPEND.getCode().equals(this.transactionType);
    }
    
    /**
     * 判断是否为转账交易
     * 
     * @return true:转账交易 false:非转账交易
     */
    public boolean isTransferTransaction() {
        return CreditTransactionType.TRANSFER.getCode().equals(this.transactionType);
    }
    
    /**
     * 判断是否为退款交易
     * 
     * @return true:退款交易 false:非退款交易
     */
    public boolean isRefundTransaction() {
        return CreditTransactionType.REFUND.getCode().equals(this.transactionType);
    }
    
    /**
     * 判断是否为管理员操作
     * 
     * @return true:管理员操作 false:非管理员操作
     */
    public boolean isAdminTransaction() {
        return CreditTransactionType.ADMIN.getCode().equals(this.transactionType);
    }
    
    /**
     * 获取交易类型枚举
     * 
     * @return 交易类型枚举，如果无效返回null
     */
    public CreditTransactionType getTransactionTypeEnum() {
        return CreditTransactionType.fromCode(this.transactionType);
    }
    
    /**
     * 获取场景编码枚举
     * 
     * @return 场景编码枚举，如果无效返回null
     */
    public CreditScenarioCode getScenarioCodeEnum() {
        return CreditScenarioCode.fromCode(this.scenarioCode);
    }
    
    /**
     * 设置用户交易主体
     * 
     * @param userId 用户ID
     */
    public void setUserSubject(Long userId) {
        this.userId = userId;
    }
} 