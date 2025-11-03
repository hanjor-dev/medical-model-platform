/**
 * 用户权限贡献实体：物化角色等来源对用户权限的贡献
 * 表：user_permission_contrib
 * 说明：用于与用户手工覆盖区分，避免互相覆盖。
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_permission_contrib")
public class UserPermissionContrib {

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 权限ID */
    private Long permissionId;

    /** 贡献来源类型（例如：ROLE） */
    private String sourceType;

    /** 贡献来源标识（例如：角色名） */
    private String sourceId;

    /** 逻辑删除标记（0:正常 1:删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 常量
    public static final String SOURCE_TYPE_ROLE = "ROLE";
}


