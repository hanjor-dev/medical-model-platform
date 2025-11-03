package com.okbug.platform.vo.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(name = "团队视图")
public class TeamVO {

    @Schema(description = "团队ID")
    private Long id;

    @Schema(description = "团队名称")
    private String teamName;

    @Schema(description = "团队码，用于邀请/加入")
    private String teamCode;

    @Schema(description = "团队描述")
    private String description;

    @Schema(description = "拥有者用户ID")
    private Long ownerUserId;

    @Schema(description = "状态(0:禁用 1:启用)")
    private Integer status;

    @Schema(description = "拥有者用户名")
    private String ownerName;

    @Schema(description = "团队成员数量（启用状态）")
    private Integer memberCount;

    @Schema(description = "团队管理员数量（ADMIN 角色，启用状态）")
    private Integer adminCount;

  @Schema(description = "当前登录用户在此团队的角色（OWNER/ADMIN/MEMBER），仅在详情接口填充")
  private String myTeamRole;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


