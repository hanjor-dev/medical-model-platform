package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(name = "接受团队邀请请求")
public class InvitationAcceptRequest {

    @NotBlank(message = "邀请令牌不能为空")
    @Schema(description = "邀请令牌", required = true)
    private String token;
}


