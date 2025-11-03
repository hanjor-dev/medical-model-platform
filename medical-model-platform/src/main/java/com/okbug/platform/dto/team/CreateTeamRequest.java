package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "创建团队请求")
public class CreateTeamRequest {

    @NotBlank(message = "团队名称不能为空")
    @Size(max = 10, message = "团队名称长度不能超过10个字符")
    @Schema(description = "团队名称", required = true)
    private String teamName;

    @Size(max = 255, message = "团队描述长度不能超过255个字符")
    @Schema(description = "团队描述", required = false)
    private String description;
}


