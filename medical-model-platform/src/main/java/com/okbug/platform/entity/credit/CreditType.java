/**
 * 积分类型实体类：对应数据库credit_types表
 * 
 * 功能描述：
 * 1. 定义系统中支持的积分类型
 * 2. 支持动态配置积分类型属性
 * 3. 控制积分类型的显示和转账权限
 * 4. 支持软删除和状态管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.entity.credit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_types")
public class CreditType {
    
    /**
     * 积分类型ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 积分类型编码（唯一，如NORMAL、PREMIUM等）
     */
    private String typeCode;
    
    /**
     * 积分类型名称
     */
    private String typeName;
    
    /**
     * 类型描述
     */
    private String description;
    
    /**
     * 积分单位名称（如：积分、高级积分、VIP点等）
     */
    private String unitName;
    
    /**
     * 积分图标URL
     */
    private String iconUrl;
    
    /**
     * 显示颜色代码
     */
    private String colorCode;
    
    /**
     * 小数位数（0表示整数）
     */
    private Integer decimalPlaces;
    
    /**
     * 是否支持转账（0:否 1:是）
     */
    private Integer isTransferable;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;
    
    /**
     * 显示排序（数值越小排序越靠前）
     */
    private Integer sortOrder;
    
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
    
    /**
     * 转账权限：不支持
     */
    public static final int TRANSFER_DISABLED = 0;
    
    /**
     * 转账权限：支持
     */
    public static final int TRANSFER_ENABLED = 1;
    
    // ================ 便利方法 ================
    
    /**
     * 判断积分类型是否已启用
     */
    public boolean isEnabled() {
        return STATUS_ENABLED == this.status;
    }
    
    /**
     * 判断积分类型是否被禁用
     */
    public boolean isDisabled() {
        return STATUS_DISABLED == this.status;
    }
    
    /**
     * 判断是否支持转账
     */
    public boolean isTransferable() {
        return TRANSFER_ENABLED == this.isTransferable;
    }
    
    /**
     * 判断是否为整数积分类型
     */
    public boolean isIntegerType() {
        return this.decimalPlaces == null || this.decimalPlaces == 0;
    }
    
    /**
     * 判断是否为小数积分类型
     */
    public boolean isDecimalType() {
        return this.decimalPlaces != null && this.decimalPlaces > 0;
    }
    
    /**
     * 启用积分类型
     */
    public void enable() {
        this.status = STATUS_ENABLED;
    }
    
    /**
     * 禁用积分类型
     */
    public void disable() {
        this.status = STATUS_DISABLED;
    }
    
    /**
     * 设置转账权限
     */
    public void setTransferable(boolean transferable) {
        this.isTransferable = transferable ? TRANSFER_ENABLED : TRANSFER_DISABLED;
    }
} 