package com.okbug.platform.vo.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "团队成员视图（包含用户基础信息）")
public class TeamMemberVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户角色")
    private String role;

    @Schema(description = "用户状态(0:禁用 1:启用)")
    private Integer userStatus;

    @Schema(description = "用户创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "团队内角色")
    private String teamRole;

    @Schema(description = "成员状态(0:禁用 1:启用)")
    private Integer status;

    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;

    @Schema(description = "用户ID（冗余字段，等同于id）")
    private Long userId;
}


