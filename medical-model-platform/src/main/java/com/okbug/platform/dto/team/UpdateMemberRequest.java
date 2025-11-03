package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;

@Data
@Schema(name = "更新团队成员请求")
public class UpdateMemberRequest {

    @Size(max = 20, message = "角色编码长度异常")
    @Schema(description = "团队内角色(OWNER/ADMIN/MEMBER)")
    private String role;

    @Schema(description = "状态(0:禁用 1:启用)")
    private Integer status;
}


