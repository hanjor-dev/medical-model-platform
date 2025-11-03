/**
 * 积分类型创建请求DTO：封装创建积分类型时的请求参数
 * 
 * 功能描述：
 * 1. 封装创建积分类型的必要参数
 * 2. 提供参数验证注解
 * 3. 支持积分类型的显示属性配置
 * 4. 确保数据安全和完整性
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:40:00
 */
package com.okbug.platform.dto.credit.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "积分类型创建请求")
public class CreditTypeCreateRequest {
    
    /**
     * 积分类型编码（必填，唯一标识）
     */
    @Schema(description = "积分类型编码", required = true, example = "VIP_POINTS")
    @NotBlank(message = "积分类型编码不能为空")
    @Size(min = 3, max = 50, message = "积分类型编码长度必须在3-50个字符之间")
    @Pattern(regexp = "^[A-Z_]+$", message = "积分类型编码只能包含大写字母和下划线")
    private String typeCode;
    
    /**
     * 积分类型名称（必填）
     */
    @Schema(description = "积分类型名称", required = true, example = "VIP积分")
    @NotBlank(message = "积分类型名称不能为空")
    @Size(min = 2, max = 100, message = "积分类型名称长度必须在2-100个字符之间")
    private String typeName;
    
    /**
     * 类型描述（可选）
     */
    @Schema(description = "类型描述", example = "VIP用户专属积分，可用于高级功能")
    @Size(max = 500, message = "类型描述长度不能超过500个字符")
    private String description;
    
    /**
     * 积分单位名称（必填）
     */
    @Schema(description = "积分单位名称", required = true, example = "VIP点")
    @NotBlank(message = "积分单位名称不能为空")
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
     * 小数位数（必填，0表示整数）
     */
    @Schema(description = "小数位数", required = true, example = "0")
    @NotNull(message = "小数位数不能为空")
    private Integer decimalPlaces;
    
    /**
     * 是否支持转账（必填）
     */
    @Schema(description = "是否支持转账", required = true, example = "true")
    @NotNull(message = "是否支持转账不能为空")
    private Boolean isTransferable;
    
    /**
     * 显示排序（可选，数值越小排序越靠前）
     */
    @Schema(description = "显示排序", example = "3")
    private Integer sortOrder;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理类型编码：去除空格，转换为大写
        if (typeCode != null) {
            typeCode = typeCode.trim().toUpperCase();
        }
        
        // 清理类型名称：去除首尾空格
        if (typeName != null) {
            typeName = typeName.trim();
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
        }
        
        // 清理图标URL：去除首尾空格
        if (iconUrl != null) {
            iconUrl = iconUrl.trim();
            if (iconUrl.isEmpty()) {
                iconUrl = null;
            }
        }
        
        // 清理颜色代码：去除空格
        if (colorCode != null) {
            colorCode = colorCode.trim();
            if (colorCode.isEmpty()) {
                colorCode = null;
            }
        }
        
        // 设置默认值
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        
        if (this.decimalPlaces == null) {
            this.decimalPlaces = 0;
        }
    }
    
    /**
     * 验证小数位数是否合理
     */
    public boolean isValidDecimalPlaces() {
        return this.decimalPlaces != null && this.decimalPlaces >= 0 && this.decimalPlaces <= 4;
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
