/**
 * 用户权限覆盖实体：对应数据库user_permissions表
 *
 * 用途：
 * - 存放用户生效权限（仅记录 ALLOW）；
 * - 超级管理员如未显式分配，运行时授予全部启用权限。
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_permissions")
public class UserPermission {

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 权限ID */
    private Long permissionId;

    /** 授权类型（ALLOW 或 DENY） */
    private String grantType;

    /** 逻辑删除标记（0:正常 1:删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ================ 常量定义 ================
    public static final String GRANT_ALLOW = "ALLOW";
    public static final String GRANT_DENY = "DENY";
}


