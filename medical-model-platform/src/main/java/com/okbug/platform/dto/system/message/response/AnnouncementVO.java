package com.okbug.platform.dto.system.message.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告返回对象
 */
@Data
@Schema(name = "公告信息")
public class AnnouncementVO {

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容(已安全过滤)")
    private String content;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "是否强制阅读")
    private Integer forceRead;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "生效时间")
    private LocalDateTime activeFrom;

    @Schema(description = "失效时间")
    private LocalDateTime activeTo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}


