package com.okbug.platform.controller.system.message;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.system.message.request.MessageQueryRequest;
import com.okbug.platform.dto.system.message.response.MessageVO;
import com.okbug.platform.dto.system.message.response.MessagePageResponse;
import com.okbug.platform.service.system.message.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckLogin;
import org.springframework.web.bind.annotation.*;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

@Tag(name = "用户-消息-站内信")
@RestController
@RequestMapping("/notify/user/message")
@RequiredArgsConstructor
@Slf4j
public class UserMessageController {

    private final MessageService messageService;

    /**
     * 我的消息分页列表
     * - 从登录上下文取用户ID
     */
    @Operation(summary = "我的消息列表")
    @GetMapping("/list")
    @SaCheckLogin
    public ApiResult<MessagePageResponse> myList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
                                             @Parameter(description = "大小") @RequestParam(defaultValue = "10") long pageSize) {
        Long uid = StpUtil.getLoginIdAsLong();
        log.info("查询我的消息列表，userId={}, pageNum={}, pageSize={}", uid, pageNum, pageSize);
        MessageQueryRequest req = new MessageQueryRequest();
        req.setUserId(uid);
        Page<MessageVO> page = messageService.page(req, pageNum, pageSize);
        long unreadTotal = messageService.countUnreadTotal(uid);
        return ApiResult.success(MessagePageResponse.of(page, unreadTotal));
    }

    @Operation(summary = "消息详情")
    @GetMapping("/{id}")
    @SaCheckLogin
    public ApiResult<MessageVO> detail(@Parameter(description = "消息ID") @PathVariable("id") Long id) {
        Long uid = StpUtil.getLoginIdAsLong();
        log.info("获取消息详情，userId={}, messageId={}", uid, id);
        MessageVO vo = messageService.getOwnDetail(id, uid);
        return ApiResult.success(vo);
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{id}/read")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "标记消息已读", async = true)
    public ApiResult<Void> read(@Parameter(description = "消息ID") @PathVariable("id") Long id) {
        Long uid = StpUtil.getLoginIdAsLong();
        log.info("标记消息为已读，userId={}, messageId={}", uid, id);
        messageService.markRead(id, uid);
        return ApiResult.success();
    }

    @Operation(summary = "全部设为已读")
    @PostMapping("/read-all")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "全部消息设为已读", async = true)
    public ApiResult<Void> readAll() {
        Long uid = StpUtil.getLoginIdAsLong();
        log.info("全部设为已读，userId={}", uid);
        messageService.markAllRead(uid);
        return ApiResult.success();
    }
}


