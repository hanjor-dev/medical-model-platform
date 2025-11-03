/**
 * 积分使用场景创建请求DTO：封装创建积分使用场景时的请求参数
 * 
 * 功能描述：
 * 1. 封装创建积分使用场景的必要参数
 * 2. 提供参数验证注解
 * 3. 支持场景规则配置
 * 4. 确保数据安全和完整性
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:10:00
 */
package com.okbug.platform.dto.credit.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Schema(description = "积分使用场景创建请求")
public class CreditScenarioCreateRequest {
    
    /**
     * 场景编码（必填，唯一标识）
     */
    @Schema(description = "场景编码", required = true, example = "AI_COMPUTE")
    @NotBlank(message = "场景编码不能为空")
    @Size(min = 3, max = 50, message = "场景编码长度必须在3-50个字符之间")
    @Pattern(regexp = "^[A-Z_]+$", message = "场景编码只能包含大写字母和下划线")
    private String scenarioCode;
    
    /**
     * 场景名称（必填）
     */
    @Schema(description = "场景名称", required = true, example = "AI计算")
    @NotBlank(message = "场景名称不能为空")
    @Size(min = 2, max = 100, message = "场景名称长度必须在2-100个字符之间")
    private String scenarioName;
    
    /**
     * 使用的积分类型编码（必填）
     */
    @Schema(description = "积分类型编码", required = true, example = "NORMAL")
    @NotBlank(message = "积分类型编码不能为空")
    @Size(min = 3, max = 50, message = "积分类型编码长度必须在3-50个字符之间")
    private String creditTypeCode;
    
    /**
     * 每次使用消耗积分数（必填，正数为消费，负数为奖励）
     */
    @Schema(description = "每次使用消耗积分数", required = true, example = "10.00")
    @NotNull(message = "每次使用消耗积分数不能为空")
    private BigDecimal costPerUse;
    
    /**
     * 场景描述（可选）
     */
    @Schema(description = "场景描述", example = "AI计算任务消耗积分")
    @Size(max = 500, message = "场景描述长度不能超过500个字符")
    private String description;
    
    /**
     * 每日使用次数限制（可选，null表示无限制）
     */
    @Schema(description = "每日使用次数限制", example = "100")
    private Integer dailyLimit;
    
    /**
     * 允许的用户角色（必填，逗号分隔）
     */
    @Schema(description = "允许的用户角色", required = true, example = "USER,ADMIN")
    @NotBlank(message = "允许的用户角色不能为空")
    @Size(max = 200, message = "用户角色长度不能超过200个字符")
    private String userRoles;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理场景编码：去除空格，转换为大写
        if (scenarioCode != null) {
            scenarioCode = scenarioCode.trim().toUpperCase();
        }
        
        // 清理场景名称：去除首尾空格
        if (scenarioName != null) {
            scenarioName = scenarioName.trim();
        }
        
        // 清理积分类型编码：去除空格，转换为大写
        if (creditTypeCode != null) {
            creditTypeCode = creditTypeCode.trim().toUpperCase();
        }
        
        // 清理描述：去除首尾空格
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
        
        // 清理用户角色：去除空格
        if (userRoles != null) {
            userRoles = userRoles.trim();
        }
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
     * 验证每日使用限制是否合理
     */
    public boolean isValidDailyLimit() {
        return this.dailyLimit == null || this.dailyLimit > 0;
    }
    
    /**
     * 验证用户角色格式是否正确
     */
    public boolean isValidUserRoles() {
        if (this.userRoles == null || this.userRoles.trim().isEmpty()) {
            return false;
        }
        
        String[] roles = this.userRoles.split(",");
        for (String role : roles) {
            if (role.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
} 
