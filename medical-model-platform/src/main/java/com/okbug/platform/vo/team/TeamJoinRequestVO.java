package com.okbug.platform.vo.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "团队加入申请视图")
public class TeamJoinRequestVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "团队ID")
    private Long teamId;

    @Schema(description = "申请人用户ID")
    private Long userId;

    @Schema(description = "申请人昵称（优先昵称，回退用户名）")
    private String applicantName;

    @Schema(description = "申请理由")
    private String requestReason;

    @Schema(description = "使用的团队码（可空）")
    private String teamCode;

    @Schema(description = "状态(0待审批 1已通过 2已拒绝)")
    private Integer status;

    @Schema(description = "处理人用户ID")
    private Long processedBy;

    @Schema(description = "处理人昵称（优先昵称，回退用户名）")
    private String processedByName;

    @Schema(description = "处理时间")
    private LocalDateTime processedAt;

    @Schema(description = "处理备注/原因")
    private String processReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


