/**
 * 积分使用场景响应DTO：封装积分使用场景查询的响应数据
 * 
 * 功能描述：
 * 1. 封装积分使用场景的完整信息
 * 2. 包含场景规则和权限信息
 * 3. 不包含敏感信息
 * 4. 用于前端展示和操作
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:20:00
 */
package com.okbug.platform.dto.credit.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@Schema(description = "积分使用场景响应")
public class CreditScenarioResponse {
    
    /**
     * 使用场景ID
     */
    @Schema(description = "使用场景ID", example = "1234567890")
    private Long id;
    
    /**
     * 场景编码
     */
    @Schema(description = "场景编码", example = "AI_COMPUTE")
    private String scenarioCode;
    
    /**
     * 场景名称
     */
    @Schema(description = "场景名称", example = "AI计算")
    private String scenarioName;
    
    /**
     * 使用的积分类型编码
     */
    @Schema(description = "积分类型编码", example = "NORMAL")
    private String creditTypeCode;
    
    /**
     * 每次使用消耗积分数
     */
    @Schema(description = "每次使用消耗积分数", example = "10.00")
    private BigDecimal costPerUse;
    
    /**
     * 场景描述
     */
    @Schema(description = "场景描述", example = "AI计算任务消耗积分")
    private String description;
    
    /**
     * 每日使用次数限制
     */
    @Schema(description = "每日使用次数限制", example = "100")
    private Integer dailyLimit;
    
    /**
     * 允许的用户角色
     */
    @Schema(description = "允许的用户角色", example = "USER,ADMIN")
    private String userRoles;
    
    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-15 12:20:00")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-15 12:20:00")
    private LocalDateTime updateTime;
    
    // ================ 便利方法 ================
    
    /**
     * 判断场景是否已启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 判断场景是否被禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
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
     * 获取状态描述
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }
    
    /**
     * 获取场景类型描述
     */
    public String getScenarioTypeText() {
        if (isConsumptionScenario()) {
            return "消费场景";
        } else if (isRewardScenario()) {
            return "奖励场景";
        } else if (isFreeScenario()) {
            return "免费场景";
        } else {
            return "未知场景";
        }
    }
    
    /**
     * 获取每日限制描述
     */
    public String getDailyLimitText() {
        if (hasDailyLimit()) {
            return "每日限制 " + dailyLimit + " 次";
        } else {
            return "无每日限制";
        }
    }
    
    /**
     * 获取用户角色列表
     */
    public List<String> getUserRoleList() {
        if (userRoles == null || userRoles.trim().isEmpty()) {
            return Arrays.asList();
        }
        
        return Arrays.stream(userRoles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * 判断指定角色是否有权限使用此场景
     */
    public boolean hasRolePermission(String role) {
        if (userRoles == null || userRoles.trim().isEmpty()) {
            return false;
        }
        
        String[] roles = userRoles.split(",");
        for (String allowedRole : roles) {
            if (allowedRole.trim().equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取场景描述（如果为空则返回默认描述）
     */
    public String getDisplayDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return "暂无描述";
    }
    
    /**
     * 获取显示名称（包含编码）
     */
    public String getDisplayName() {
        if (scenarioName != null && scenarioCode != null) {
            return scenarioName + " (" + scenarioCode + ")";
        }
        return scenarioName != null ? scenarioName : scenarioCode;
    }
    
    /**
     * 获取积分消耗/奖励描述
     */
    public String getCostDescription() {
        if (costPerUse == null) {
            return "未设置";
        }
        
        if (isConsumptionScenario()) {
            return "消耗 " + costPerUse + " 积分";
        } else if (isRewardScenario()) {
            return "奖励 " + costPerUse.abs() + " 积分";
        } else {
            return "免费使用";
        }
    }
} 
