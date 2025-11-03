package com.okbug.platform.dto.system.message.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "消息分页响应（包含未读总数）")
public class MessagePageResponse {

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页大小")
    private long size;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "总页数")
    private long pages;

    @Schema(description = "记录列表")
    private List<MessageVO> records;

    @Schema(description = "该用户全部消息的未读总数")
    private long unreadTotal;

    public static MessagePageResponse of(Page<MessageVO> page, long unreadTotal) {
        MessagePageResponse resp = new MessagePageResponse();
        if (page != null) {
            resp.setCurrent(page.getCurrent());
            resp.setSize(page.getSize());
            resp.setTotal(page.getTotal());
            resp.setPages(page.getPages());
            resp.setRecords(page.getRecords());
        }
        resp.setUnreadTotal(unreadTotal);
        return resp;
    }
}


