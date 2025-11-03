package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "提交加入申请请求")
public class JoinRequestSubmit {

    @NotNull(message = "团队ID不能为空")
    @Schema(description = "团队ID", required = true)
    private Long teamId;

    @Size(max = 64, message = "团队码长度不能超过64")
    @Schema(description = "团队码，可选")
    private String teamCode;

    @Size(max = 200, message = "申请理由长度不能超过200")
    @Schema(description = "申请理由，可选")
    private String requestReason;
}


