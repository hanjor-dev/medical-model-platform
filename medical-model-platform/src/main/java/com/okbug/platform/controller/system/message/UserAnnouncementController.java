package com.okbug.platform.controller.system.message;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.system.message.response.AnnouncementVO;
import com.okbug.platform.service.system.message.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

/**
 * 用户端 - 公告接口（系统/消息）
 */
@Slf4j
@Tag(name = "用户端-公告")
@RestController
@RequestMapping("/notify/announcement")
@RequiredArgsConstructor
public class UserAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "可见公告分页")
    @GetMapping("/visible")
    @SaCheckLogin
    public ApiResult<Page<AnnouncementVO>> pageVisible(@Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
                                                       @Parameter(description = "大小") @RequestParam(defaultValue = "10") long pageSize) {
        log.info("查询可见公告，pageNum={}, pageSize={}", pageNum, pageSize);
        Page<AnnouncementVO> page = announcementService.pageVisible(pageNum, pageSize);
        return ApiResult.success(page);
    }

    @Operation(summary = "未读且可见公告分页")
    @GetMapping("/visible/unread")
    @SaCheckLogin
    public ApiResult<Page<AnnouncementVO>> pageVisibleUnread(@Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
                                                             @Parameter(description = "大小") @RequestParam(defaultValue = "10") long pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("查询未读且可见公告，userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        Page<AnnouncementVO> page = announcementService.pageVisibleUnread(userId, pageNum, pageSize);
        return ApiResult.success(page);
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    @SaCheckLogin
    public ApiResult<AnnouncementVO> detail(@Parameter(description = "公告ID") @PathVariable("id") Long id) {
        log.info("获取公告详情，id={}", id);
        AnnouncementVO vo = announcementService.getById(id);
        return ApiResult.success(vo);
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{id}/read")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "标记公告已读", async = true)
    public ApiResult<Void> read(@Parameter(description = "公告ID") @PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("标记公告已读，userId={}, id={}", userId, id);
        announcementService.read(id, userId);
        return ApiResult.success();
    }
}


