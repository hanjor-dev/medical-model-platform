package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "更新团队请求")
public class UpdateTeamRequest {

    @NotNull(message = "团队ID不能为空")
    @Schema(description = "团队ID", required = true)
    private Long id;

    @Size(max = 10, message = "团队名称长度不能超过10个字符")
    @Schema(description = "团队名称", required = false)
    private String teamName;

    @Size(max = 255, message = "团队描述长度不能超过255个字符")
    @Schema(description = "团队描述", required = false)
    private String description;

    @Schema(description = "团队状态：1启用，0禁用", required = false)
    private Integer status;
}


