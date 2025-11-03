/**
 * 用户积分账户实体类：对应数据库user_credits表
 * 
 * 功能描述：
 * 1. 管理用户在不同积分类型下的账户信息
 * 2. 记录积分余额和累计统计
 * 3. 支持乐观锁防止并发操作
 * 4. 支持软删除和审计字段
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:10:00
 */
package com.okbug.platform.entity.credit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_credits")
public class UserCredit {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分类型编码
     */
    private String creditTypeCode;
    
    /**
     * 积分余额
     */
    private BigDecimal balance;
    
    /**
     * 累计获得积分
     */
    private BigDecimal totalEarned;
    
    /**
     * 累计消费积分
     */
    private BigDecimal totalConsumed;
    
    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version = 0;
    
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
    
    // ================ 便利方法 ================
    
    /**
     * 判断积分余额是否足够
     */
    public boolean hasSufficientBalance(BigDecimal requiredAmount) {
        if (this.balance == null || requiredAmount == null) {
            return false;
        }
        return this.balance.compareTo(requiredAmount) >= 0;
    }
    
    /**
     * 判断积分余额是否不足
     */
    public boolean hasInsufficientBalance(BigDecimal requiredAmount) {
        return !hasSufficientBalance(requiredAmount);
    }
    
    /**
     * 增加积分余额
     */
    public void addBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            this.balance = this.balance.add(amount);
            if (this.totalEarned == null) {
                this.totalEarned = BigDecimal.ZERO;
            }
            this.totalEarned = this.totalEarned.add(amount);
        }
    }
    
    /**
     * 减少积分余额
     */
    public void subtractBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            this.balance = this.balance.subtract(amount);
            if (this.totalConsumed == null) {
                this.totalConsumed = BigDecimal.ZERO;
            }
            this.totalConsumed = this.totalConsumed.add(amount);
        }
    }
    
    /**
     * 设置初始积分余额
     */
    public void setInitialBalance(BigDecimal initialAmount) {
        if (initialAmount != null && initialAmount.compareTo(BigDecimal.ZERO) >= 0) {
            this.balance = initialAmount;
            this.totalEarned = initialAmount;
            this.totalConsumed = BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取可用余额（如果余额为null则返回0）
     */
    public BigDecimal getAvailableBalance() {
        return this.balance != null ? this.balance : BigDecimal.ZERO;
    }
    
    /**
     * 获取累计获得积分（如果为null则返回0）
     */
    public BigDecimal getTotalEarnedAmount() {
        return this.totalEarned != null ? this.totalEarned : BigDecimal.ZERO;
    }
    
    /**
     * 获取累计消费积分（如果为null则返回0）
     */
    public BigDecimal getTotalConsumedAmount() {
        return this.totalConsumed != null ? this.totalConsumed : BigDecimal.ZERO;
    }
    
    /**
     * 判断账户是否为空账户（无积分余额）
     */
    public boolean isEmptyAccount() {
        return this.balance == null || this.balance.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 判断账户是否有积分余额
     */
    public boolean hasBalance() {
        return !isEmptyAccount();
    }
} 