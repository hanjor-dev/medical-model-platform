package com.okbug.platform.dto.system.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告查询请求
 */
@Data
@Schema(name = "公告查询请求")
public class AnnouncementQueryRequest {

    @Schema(description = "状态(0:草稿 1:已发布 2:已下线)")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime fromTime;

    @Schema(description = "结束时间")
    private LocalDateTime toTime;
}


