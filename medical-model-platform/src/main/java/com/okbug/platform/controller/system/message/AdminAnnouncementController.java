package com.okbug.platform.controller.system.message;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.system.message.request.AnnouncementCreateRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementQueryRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementUpdateRequest;
import com.okbug.platform.dto.system.message.response.AnnouncementVO;
import com.okbug.platform.service.system.message.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

/**
 * 管理端 - 公告管理接口（系统/消息）
 */
@Slf4j
@Validated
@Tag(name = "系统管理-消息-公告管理")
@RestController
@RequestMapping("/notify/admin/announcement")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "创建公告(草稿)")
    @PostMapping
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.CREATE, description = "创建公告(草稿)", async = true)
    public ApiResult<Long> create(@RequestBody @Validated AnnouncementCreateRequest request) {
        Long id = announcementService.create(request, null, null);
        return ApiResult.success("创建成功", id);
    }

    @Operation(summary = "更新公告(草稿)")
    @PutMapping("/{id}")
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "更新公告(草稿)", async = true)
    public ApiResult<Void> update(@Parameter(description = "公告ID") @PathVariable Long id,
                                  @RequestBody @Validated AnnouncementUpdateRequest request) {
        announcementService.update(id, request, null, null);
        return ApiResult.success("更新成功");
    }

    @Operation(summary = "发布公告")
    @PostMapping("/{id}/publish")
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.ENABLE, description = "发布公告", async = true)
    public ApiResult<Void> publish(@Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.publish(id, null, null);
        return ApiResult.success("发布成功");
    }

    @Operation(summary = "下线公告")
    @PostMapping("/{id}/offline")
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.DISABLE, description = "下线公告", async = true)
    public ApiResult<Void> offline(@Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.offline(id, null, null);
        return ApiResult.success("下线成功");
    }

    @Operation(summary = "公告分页（管理端）")
    @GetMapping("/list")
    @SaCheckRole("SUPER_ADMIN")
    public ApiResult<Page<AnnouncementVO>> page(AnnouncementQueryRequest request,
                                               @Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
                                               @Parameter(description = "大小") @RequestParam(defaultValue = "10") long pageSize) {
        Page<AnnouncementVO> page = announcementService.pageAdmin(request, pageNum, pageSize);
        return ApiResult.success(page);
    }
}


