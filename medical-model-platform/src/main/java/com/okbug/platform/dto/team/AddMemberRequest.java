package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(name = "添加团队成员请求")
public class AddMemberRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @NotBlank(message = "团队内角色不能为空")
    @Schema(description = "团队内角色(OWNER/ADMIN/MEMBER)", required = true)
    private String role;
}


