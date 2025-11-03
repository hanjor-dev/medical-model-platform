package com.okbug.platform.dto.system.message.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "消息展示对象")
public class MessageVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "消息类型")
    private String messageType;
    @Schema(description = "消息类型中文")
    private String messageTypeLabel;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "计划时间")
    private LocalDateTime scheduleTime;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否已读")
    private Boolean isRead;
}


