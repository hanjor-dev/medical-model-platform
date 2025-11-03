package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(name = "设置团队状态请求（SUPER_ADMIN 専用）")
public class SetTeamStatusRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "团队状态：1启用，0禁用", required = true)
    private Integer status;
}


