package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@Schema(name = "更新团队配置请求")
public class UpdateTeamConfigRequest {

    @NotNull(message = "团队ID不能为空")
    @Schema(description = "团队ID", required = true)
    private Long teamId;

    @Schema(description = "配置键值对", required = true)
    private Map<String, String> configs;
}


