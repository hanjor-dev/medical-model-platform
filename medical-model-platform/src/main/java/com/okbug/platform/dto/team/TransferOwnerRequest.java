package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(name = "转移拥有者请求")
public class TransferOwnerRequest {

    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "转移后新的拥有者用户ID")
    private Long toUserId;
}


