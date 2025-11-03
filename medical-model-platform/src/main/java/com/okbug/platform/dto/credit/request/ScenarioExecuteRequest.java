package com.okbug.platform.dto.credit.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(name = "按场景执行积分操作请求")
public class ScenarioExecuteRequest {

    @NotBlank(message = "场景编码不能为空")
    @Schema(description = "使用场景编码", required = true, example = "AI_COMPUTE")
    private String scenarioCode;

    @Schema(description = "关联订单号（消费建议必传，用于幂等/退款）", example = "ORDER-202501150001")
    private String relatedOrderId;

    @Schema(description = "关联用户ID（奖励时可用，如推荐人）", example = "123")
    private Long relatedUserId;

    @Schema(description = "描述信息", example = "AI计算预扣")
    private String description;
}


