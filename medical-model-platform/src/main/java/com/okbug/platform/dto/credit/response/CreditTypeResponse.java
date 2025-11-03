/**
 * 积分类型响应DTO：封装积分类型查询的响应数据
 * 
 * 功能描述：
 * 1. 封装积分类型的完整信息
 * 2. 包含显示属性和状态信息
 * 3. 不包含敏感信息
 * 4. 用于前端展示和操作
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:50:00
 */
package com.okbug.platform.dto.credit.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(description = "积分类型响应")
public class CreditTypeResponse {
    
    /**
     * 积分类型ID
     */
    @Schema(description = "积分类型ID", example = "1234567890")
    private Long id;
    
    /**
     * 积分类型编码
     */
    @Schema(description = "积分类型编码", example = "VIP_POINTS")
    private String typeCode;
    
    /**
     * 积分类型名称
     */
    @Schema(description = "积分类型名称", example = "VIP积分")
    private String typeName;
    
    /**
     * 类型描述
     */
    @Schema(description = "类型描述", example = "VIP用户专属积分，可用于高级功能")
    private String description;
    
    /**
     * 积分单位名称
     */
    @Schema(description = "积分单位名称", example = "VIP点")
    private String unitName;
    
    /**
     * 积分图标URL
     */
    @Schema(description = "积分图标URL", example = "/icons/vip-credit.png")
    private String iconUrl;
    
    /**
     * 显示颜色代码
     */
    @Schema(description = "显示颜色代码", example = "#FF6B6B")
    private String colorCode;
    
    /**
     * 小数位数
     */
    @Schema(description = "小数位数", example = "0")
    private Integer decimalPlaces;
    
    /**
     * 是否支持转账
     */
    @Schema(description = "是否支持转账", example = "true")
    private Boolean isTransferable;
    
    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    /**
     * 显示排序
     */
    @Schema(description = "显示排序", example = "3")
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-15 10:50:00")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-15 10:50:00")
    private LocalDateTime updateTime;
    
    // ================ 便利方法 ================
    
    /**
     * 判断积分类型是否已启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 判断积分类型是否被禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }
    
    /**
     * 判断是否支持转账
     */
    public boolean isTransferable() {
        return isTransferable != null && isTransferable;
    }
    
    /**
     * 判断是否为整数类型
     */
    public boolean isIntegerType() {
        return decimalPlaces != null && decimalPlaces == 0;
    }
    
    /**
     * 判断是否为小数类型
     */
    public boolean isDecimalType() {
        return decimalPlaces != null && decimalPlaces > 0;
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
     * 获取转账权限描述
     */
    public String getTransferableText() {
        if (isTransferable == null) {
            return "未知";
        }
        return isTransferable ? "支持" : "不支持";
    }
    
    /**
     * 获取类型描述（如果为空则返回默认描述）
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
        if (typeName != null && typeCode != null) {
            return typeName + " (" + typeCode + ")";
        }
        return typeName != null ? typeName : typeCode;
    }
} 
