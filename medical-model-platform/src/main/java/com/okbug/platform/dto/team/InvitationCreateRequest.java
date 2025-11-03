package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "创建团队邀请请求")
public class InvitationCreateRequest {

    @NotNull(message = "团队ID不能为空")
    @Schema(description = "团队ID", required = true)
    private Long teamId;

    @Size(max = 100, message = "邮箱长度不能超过100")
    @Schema(description = "被邀请邮箱，可选")
    private String invitedEmail;

    @Size(max = 30, message = "手机号长度不能超过30")
    @Schema(description = "被邀请手机号，可选")
    private String invitedPhone;

    @Schema(description = "被邀请用户ID，可选")
    private Long invitedUserId;

    @Size(max = 20, message = "角色长度不能超过20")
    @Schema(description = "团队角色，默认MEMBER")
    private String role;
}


