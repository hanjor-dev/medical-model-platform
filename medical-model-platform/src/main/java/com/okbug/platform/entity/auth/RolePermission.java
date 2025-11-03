/**
 * 角色权限关联实体类：对应数据库role_permissions表
 * 
 * 功能描述：
 * 1. 存储角色和权限的关联关系
 * 2. 支持角色权限的动态配置
 * 3. 用于权限验证和菜单构建
 * 4. 软删除和审计字段支持
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:30:00
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("role_permissions")
public class RolePermission {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 角色（USER、ADMIN、SUPER_ADMIN）
     */
    private String role;
    
    /**
     * 权限ID
     */
    private Long permissionId;
    
    /**
     * 删除标记
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
     * 创建角色权限关联
     */
    public static RolePermission create(String role, Long permissionId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermissionId(permissionId);
        return rolePermission;
    }
} 