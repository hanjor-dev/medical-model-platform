/**
 * 积分分配请求DTO：封装管理员向用户分配积分的请求参数
 * 
 * 功能描述：
 * 1. 封装积分分配的必要参数
 * 2. 提供参数验证注解
 * 3. 支持积分类型和数量配置
 * 4. 确保数据安全和完整性
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:20:00
 */
package com.okbug.platform.dto.credit.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Schema(description = "积分分配请求")
public class CreditAllocateRequest {
    
    /**
     * 目标用户ID（必填）
     */
    @Schema(description = "目标用户ID", required = true, example = "1234567890")
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;
    
    /**
     * 积分类型编码（必填）
     */
    @Schema(description = "积分类型编码", required = true, example = "NORMAL")
    @NotBlank(message = "积分类型编码不能为空")
    @Size(min = 3, max = 50, message = "积分类型编码长度必须在3-50个字符之间")
    private String creditTypeCode;
    
    /**
     * 分配积分数（必填，必须大于0）
     */
    @Schema(description = "分配积分数", required = true, example = "1000.00")
    @NotNull(message = "分配积分数不能为空")
    @Positive(message = "分配积分数必须大于0")
    private BigDecimal amount;
    
    /**
     * 分配描述（可选）
     */
    @Schema(description = "分配描述", example = "月度积分分配")
    @Size(max = 200, message = "分配描述长度不能超过200个字符")
    private String description;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理积分类型编码：去除空格，转换为大写
        if (creditTypeCode != null) {
            creditTypeCode = creditTypeCode.trim().toUpperCase();
        }
        
        // 清理分配描述：去除首尾空格
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
    }
    
    /**
     * 验证分配积分数是否合理
     */
    public boolean isValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 获取分配积分数（如果为null则返回0）
     */
    public BigDecimal getAmountOrDefault() {
        return amount != null ? amount : BigDecimal.ZERO;
    }
    
    /**
     * 判断是否有分配描述
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
} 
