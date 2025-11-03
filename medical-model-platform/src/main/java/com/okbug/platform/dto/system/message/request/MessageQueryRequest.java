package com.okbug.platform.dto.system.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "消息查询请求")
public class MessageQueryRequest {

    @Schema(description = "接收用户ID，可选")
    private Long userId;

    @Schema(description = "消息类型编码，可选")
    private String messageType;

    @Schema(description = "状态，可选")
    private Integer status;
}


