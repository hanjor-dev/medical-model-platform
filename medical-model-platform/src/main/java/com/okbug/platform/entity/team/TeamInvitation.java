/**
 * 团队邀请实体：对应数据库表 team_invitations
 *
 * 功能描述：
 * 1. 记录团队邀请信息（可通过用户ID、邮箱或手机号邀请）
 * 2. 维护邀请令牌与过期时间
 * 3. 状态流转：0待接受 1已接受 2已拒绝 3已过期
 */
package com.okbug.platform.entity.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("team_invitations")
public class TeamInvitation {

    /** 邀请ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 团队ID */
    private Long teamId;

    /** 被邀请用户ID（可为空） */
    private Long invitedUserId;

    /** 被邀请邮箱（可为空） */
    private String invitedEmail;

    /** 被邀请手机号（可为空） */
    private String invitedPhone;

    /** 邀请人用户ID */
    private Long inviterUserId;

    /** 邀请的团队角色(默认MEMBER) */
    private String teamRole;

    /** 邀请令牌（唯一） */
    private String invitationToken;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 状态(0:待接受 1:已接受 2:已拒绝 3:已过期) */
    private Integer status;

    /** 接受时间 */
    private LocalDateTime acceptedAt;

    /** 逻辑删除(0:正常 1:删除) */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ================ 状态常量 ================
    public static final int PENDING = 0;
    public static final int ACCEPTED = 1;
    public static final int REJECTED = 2;
    public static final int EXPIRED = 3;
}


