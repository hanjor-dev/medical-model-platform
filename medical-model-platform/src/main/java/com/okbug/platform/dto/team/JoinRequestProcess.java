package com.okbug.platform.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Schema(name = "审批加入申请请求")
public class JoinRequestProcess {

    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID", required = true)
    private Long requestId;

    @NotNull(message = "处理结果不能为空")
    @Schema(description = "是否通过", required = true)
    private Boolean approve;

    @Size(max = 200, message = "处理理由长度不能超过200")
    @Schema(description = "处理理由，可选")
    private String reason;
}


