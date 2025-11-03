/**
 * 团队加入申请实体：对应数据库表 team_join_requests
 *
 * 功能描述：
 * 1. 记录用户申请加入团队的请求
 * 2. 支持审批人、审批时间与处理原因
 * 3. 状态流转：0待审批 1已通过 2已拒绝
 */
package com.okbug.platform.entity.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("team_join_requests")
public class TeamJoinRequest {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 团队ID */
    private Long teamId;

    /** 申请用户ID */
    private Long userId;

    /** 使用的团队码（可空） */
    private String teamCode;

    /** 申请理由 */
    private String requestReason;

    /** 状态(0:待审批 1:已通过 2:已拒绝) */
    private Integer status;

    /** 处理人用户ID */
    private Long processedBy;

    /** 处理时间 */
    private LocalDateTime processedAt;

    /** 处理理由 */
    private String processReason;

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
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;
}


