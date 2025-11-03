/**
 * 团队成员实体：对应数据库表 team_members
 *
 * 功能描述：
 * 1. 维护用户与团队的成员关系
 * 2. 支持团队内角色标识（OWNER/ADMIN/MEMBER）
 * 3. 支持软删除与审计字段
 *
 * 代码规范：
 * - 不返回实体到Controller以外，Controller统一使用ApiResult
 * - 复杂查询通过Service构造Wrapper，不在Mapper写SQL
 */
package com.okbug.platform.entity.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("team_members")
public class TeamMember {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 团队ID */
    private Long teamId;

    /** 用户ID */
    private Long userId;

    /** 团队内角色(OWNER/ADMIN/MEMBER) */
    private String teamRole;

    /** 状态(0:禁用 1:启用) */
    private Integer status;

    /** 加入时间 */
    private LocalDateTime joinedAt;

    /** 逻辑删除(0:正常 1:删除) */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.IGNORED)
    private Long createBy;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE, insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Long updateBy;

    // ================ 角色常量 ================
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    // ================ 状态常量 ================
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
}


