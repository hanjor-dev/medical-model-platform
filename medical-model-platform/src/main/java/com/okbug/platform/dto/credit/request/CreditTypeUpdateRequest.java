/**
 * 积分类型更新请求DTO：封装更新积分类型时的请求参数
 * 
 * 功能描述：
 * 1. 封装更新积分类型的可选参数
 * 2. 提供参数验证注解
 * 3. 支持部分字段更新
 * 4. 确保数据安全和完整性
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:45:00
 */
package com.okbug.platform.dto.credit.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "积分类型更新请求")
public class CreditTypeUpdateRequest {
    
    /**
     * 积分类型名称（可选）
     */
    @Schema(description = "积分类型名称", example = "VIP积分")
    @Size(min = 2, max = 100, message = "积分类型名称长度必须在2-100个字符之间")
    private String typeName;
    
    /**
     * 类型描述（可选）
     */
    @Schema(description = "类型描述", example = "VIP用户专属积分，可用于高级功能")
    @Size(max = 500, message = "类型描述长度不能超过500个字符")
    private String description;
    
    /**
     * 积分单位名称（可选）
     */
    @Schema(description = "积分单位名称", example = "VIP点")
    @Size(min = 1, max = 20, message = "积分单位名称长度必须在1-20个字符之间")
    private String unitName;
    
    /**
     * 积分图标URL（可选）
     */
    @Schema(description = "积分图标URL", example = "/icons/vip-credit.png")
    @Size(max = 500, message = "积分图标URL长度不能超过500个字符")
    private String iconUrl;
    
    /**
     * 显示颜色代码（可选）
     */
    @Schema(description = "显示颜色代码", example = "#FF6B6B")
    @Pattern(regexp = "^$|^\\s*$|^#[0-9A-Fa-f]{6}$", message = "颜色代码格式不正确，应为#RRGGBB格式")
    private String colorCode;
    
    /**
     * 小数位数（可选，0表示整数）
     */
    @Schema(description = "小数位数", example = "0")
    private Integer decimalPlaces;
    
    /**
     * 是否支持转账（可选）
     */
    @Schema(description = "是否支持转账", example = "true")
    private Boolean isTransferable;
    
    /**
     * 显示排序（可选，数值越小排序越靠前）
     */
    @Schema(description = "显示排序", example = "3")
    private Integer sortOrder;
    
    /**
     * 状态（可选，0:禁用 1:启用）
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理类型名称：去除首尾空格
        if (typeName != null) {
            typeName = typeName.trim();
            if (typeName.isEmpty()) {
                typeName = null;
            }
        }
        
        // 清理描述：去除首尾空格
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
        
        // 清理单位名称：去除首尾空格
        if (unitName != null) {
            unitName = unitName.trim();
            if (unitName.isEmpty()) {
                unitName = null;
            }
        }
        
        // 清理图标URL：去除首尾空格
        if (iconUrl != null) {
            iconUrl = iconUrl.trim();
            if (iconUrl.isEmpty()) {
                iconUrl = null;
            }
        }
        
        // 清理颜色代码：去除空格（保留空字符串，表示明确清空）
        if (colorCode != null) {
            colorCode = colorCode.trim();
        }
    }
    
    /**
     * 验证小数位数是否合理
     */
    public boolean isValidDecimalPlaces() {
        return this.decimalPlaces == null || (this.decimalPlaces >= 0 && this.decimalPlaces <= 4);
    }
    
    /**
     * 判断是否有需要更新的字段
     */
    public boolean hasUpdateFields() {
        return typeName != null || description != null || unitName != null || 
               iconUrl != null || colorCode != null || decimalPlaces != null || 
               isTransferable != null || sortOrder != null || status != null;
    }
    
    /**
     * 判断是否为整数类型
     */
    public boolean isIntegerType() {
        return this.decimalPlaces != null && this.decimalPlaces == 0;
    }
    
    /**
     * 判断是否为小数类型
     */
    public boolean isDecimalType() {
        return this.decimalPlaces != null && this.decimalPlaces > 0;
    }
} 
