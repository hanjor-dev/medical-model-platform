package com.okbug.platform.dto.system.message.request;

import com.okbug.platform.domain.notify.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(name = "创建站内消息请求")
public class MessageCreateRequest {

    @Schema(description = "接收用户ID")
    @NotNull(message = "接收用户不能为空")
    private Long userId;

    @Schema(description = "消息类型，可选")
    private MessageType messageType;

    @Schema(description = "标题，可选")
    @Length(max = 128, message = "标题最长128字符")
    private String title;

    @Schema(description = "正文内容，必填")
    @NotNull(message = "内容不能为空")
    private String content;

    @Schema(description = "计划推送时间，null表示立即")
    private LocalDateTime scheduleTime;
}


