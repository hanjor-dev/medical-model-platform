package com.okbug.platform.dto.system.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 更新公告请求参数
 */
@Data
@Schema(name = "更新公告请求")
public class AnnouncementUpdateRequest {

    @Schema(description = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Schema(description = "富文本内容", required = true)
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "优先级，越大越靠前", required = true)
    private Integer priority;

    @Schema(description = "是否强制阅读(0:否 1:是)")
    private Integer forceRead;

    @Schema(description = "生效时间，可空")
    private LocalDateTime activeFrom;

    @Schema(description = "失效时间，可空")
    private LocalDateTime activeTo;
}


