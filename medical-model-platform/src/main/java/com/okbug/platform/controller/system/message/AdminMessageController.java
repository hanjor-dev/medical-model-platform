package com.okbug.platform.controller.system.message;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.system.message.request.MessageCreateRequest;
import com.okbug.platform.dto.system.message.request.MessageQueryRequest;
import com.okbug.platform.dto.system.message.response.MessageVO;
import com.okbug.platform.service.system.message.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

@Validated
@Tag(name = "系统管理-消息-站内消息管理")
@RestController
@RequestMapping("/notify/admin/message")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;

    /**
     * 创建站内消息
     * - 仅 SUPER_ADMIN 可用
     * - 入参经 @Validated 校验
     * - 返回 ApiResult<Long> 为消息ID
     */
    @Operation(summary = "创建站内消息")
    @PostMapping
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.CREATE, description = "创建站内消息", async = true)
    public ApiResult<Long> create(@RequestBody @Validated MessageCreateRequest request) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        Long id = messageService.create(request, operatorId, null);
        return ApiResult.success("创建成功", id);
    }

    /**
     * 取消站内消息
     * - 仅 SUPER_ADMIN 可用
     */
    @Operation(summary = "取消站内消息")
    @PostMapping("/{id}/cancel")
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.DELETE, description = "取消站内消息", async = true)
    public ApiResult<Void> cancel(@Parameter(description = "消息ID") @PathVariable Long id) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        messageService.cancel(id, operatorId, null);
        return ApiResult.success("取消成功");
    }

    /**
     * 消息分页
     * - 仅 SUPER_ADMIN 可用
     */
    @Operation(summary = "消息分页")
    @GetMapping("/list")
    @SaCheckRole("SUPER_ADMIN")
    public ApiResult<Page<MessageVO>> page(MessageQueryRequest request,
                                           @Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
                                           @Parameter(description = "大小") @RequestParam(defaultValue = "10") long pageSize) {
        Page<MessageVO> page = messageService.page(request, pageNum, pageSize);
        return ApiResult.success(page);
    }
}


