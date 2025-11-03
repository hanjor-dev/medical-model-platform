/**
 * 权限实体类：对应数据库permissions表
 * 
 * 功能描述：
 * 1. 系统权限配置存储
 * 2. 支持菜单、按钮、接口三种权限类型
 * 3. 支持树形结构的权限层级
 * 4. 软删除和排序支持
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:00:00
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("permissions")
public class Permission {
    
    /**
     * 权限ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 权限编码（唯一）
     */
    private String permissionCode;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限类型（MENU:菜单 BUTTON:按钮 API:接口）
     */
    private String permissionType;
    
    /**
     * 父权限ID（用于构建权限树）
     */
    private Long parentId;
    
    /**
     * 菜单路径（前端路由路径）
     */
    private String path;
    
    /**
     * 组件路径（前端组件路径）
     */
    private String component;
    
    /**
     * 图标（菜单图标）
     */
    private String icon;
    
    /**
     * 排序号
     */
    private Integer sort;
    
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
     * 权限类型：菜单
     */
    public static final String TYPE_MENU = "MENU";
    
    /**
     * 权限类型：按钮
     */
    public static final String TYPE_BUTTON = "BUTTON";
    
    /**
     * 权限类型：接口
     */
    public static final String TYPE_API = "API";
    
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
     * 判断是否为菜单权限
     */
    public boolean isMenu() {
        return TYPE_MENU.equals(this.permissionType);
    }
    
    /**
     * 判断是否为按钮权限
     */
    public boolean isButton() {
        return TYPE_BUTTON.equals(this.permissionType);
    }
    
    /**
     * 判断是否为接口权限
     */
    public boolean isApi() {
        return TYPE_API.equals(this.permissionType);
    }
    
    /**
     * 判断是否为顶级权限（没有父权限）
     */
    public boolean isRoot() {
        return this.parentId == null || this.parentId == 0;
    }
    
    /**
     * 判断权限是否已启用
     */
    public boolean isEnabled() {
        return STATUS_ENABLED == this.status;
    }
    
    /**
     * 判断权限是否被禁用
     */
    public boolean isDisabled() {
        return STATUS_DISABLED == this.status;
    }
} 