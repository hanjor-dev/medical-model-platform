/**
 * 积分使用场景实体类：对应数据库credit_usage_scenarios表
 * 
 * 功能描述：
 * 1. 定义积分使用的业务场景
 * 2. 配置每个场景的积分消耗和奖励规则
 * 3. 控制用户角色权限和使用限制
 * 4. 支持动态配置和状态管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:05:00
 */
package com.okbug.platform.entity.credit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_usage_scenarios")
public class CreditUsageScenario {
    
    /**
     * 场景ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 场景编码（唯一，如AI_COMPUTE、READING、UPLOAD等）
     */
    private String scenarioCode;
    
    /**
     * 场景名称
     */
    private String scenarioName;
    
    /**
     * 使用的积分类型编码
     */
    private String creditTypeCode;
    
    /**
     * 每次使用消耗积分数（正数为消费，负数为奖励）
     */
    private BigDecimal costPerUse;
    
    /**
     * 场景描述
     */
    private String description;
    
    /**
     * 每日使用次数限制（null表示无限制）
     */
    private Integer dailyLimit;
    
    /**
     * 允许的用户角色（逗号分隔，如USER,ADMIN,SUPER_ADMIN）
     */
    private String userRoles;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;
    
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
    
    // ================ 常量定义 ================
    
    /**
     * 状态：禁用
     */
    public static final int STATUS_DISABLED = 0;
    
    /**
     * 状态：启用
     */
    public static final int STATUS_ENABLED = 1;
    
    // ================ 便利方法 ================
    
    /**
     * 判断场景是否已启用
     */
    public boolean isEnabled() {
        return STATUS_ENABLED == this.status;
    }
    
    /**
     * 判断场景是否被禁用
     */
    public boolean isDisabled() {
        return STATUS_DISABLED == this.status;
    }
    
    /**
     * 判断是否为消费场景（消耗积分）
     */
    public boolean isConsumptionScenario() {
        return this.costPerUse != null && this.costPerUse.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 判断是否为奖励场景（获得积分）
     */
    public boolean isRewardScenario() {
        return this.costPerUse != null && this.costPerUse.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * 判断是否为免费场景（不消耗积分）
     */
    public boolean isFreeScenario() {
        return this.costPerUse != null && this.costPerUse.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 判断是否有每日使用限制
     */
    public boolean hasDailyLimit() {
        return this.dailyLimit != null && this.dailyLimit > 0;
    }
    
    /**
     * 判断是否无每日使用限制
     */
    public boolean hasNoDailyLimit() {
        return this.dailyLimit == null || this.dailyLimit <= 0;
    }
    
    /**
     * 获取消费积分数（正数）
     */
    public BigDecimal getConsumptionAmount() {
        if (isConsumptionScenario()) {
            return this.costPerUse;
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 获取奖励积分数（正数）
     */
    public BigDecimal getRewardAmount() {
        if (isRewardScenario()) {
            return this.costPerUse.abs();
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 判断指定角色是否有权限使用此场景
     */
    public boolean hasRolePermission(String role) {
        if (this.userRoles == null || this.userRoles.trim().isEmpty()) {
            return false;
        }
        String[] roles = this.userRoles.split(",");
        for (String allowedRole : roles) {
            if (allowedRole.trim().equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 启用场景
     */
    public void enable() {
        this.status = STATUS_ENABLED;
    }
    
    /**
     * 禁用场景
     */
    public void disable() {
        this.status = STATUS_DISABLED;
    }
} 