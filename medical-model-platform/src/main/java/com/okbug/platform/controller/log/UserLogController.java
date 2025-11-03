/**
 * 用户日志管理控制器
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:00:00
 */
package com.okbug.platform.controller.log;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.log.LogQueryRequest;
import com.okbug.platform.dto.log.LogResponse;
import com.okbug.platform.service.log.UserLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/user-logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户日志管理")
public class UserLogController {
    
    private final UserLogService userLogService;

    /**
     * 分页查询用户日志
     */
    @GetMapping
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "分页查询用户日志", description = "支持登录日志、操作日志统一查询和筛选")
    public ApiResult<IPage<LogResponse>> queryLogs(@Valid LogQueryRequest request) {
        log.info("查询用户日志，参数: {}", request);
        IPage<LogResponse> result = userLogService.queryLogs(request);
        return ApiResult.success(result);
    }
    
    /**
     * 获取日志详情
     */
    @GetMapping("/{logId}")
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "获取日志详情", description = "根据ID获取日志详细信息")
    public ApiResult<LogResponse> getLogDetail(
            @Parameter(description = "日志ID") @PathVariable Long logId) {
        log.info("获取日志详情，ID: {}", logId);
        LogResponse result = userLogService.getLogDetail(logId);
        return ApiResult.success(result);
    }
    
    /**
     * 获取操作模块选项
     */
    @GetMapping("/modules")
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "获取操作模块选项", description = "获取系统中所有操作模块的选项列表（label中文，value英文）")
    public ApiResult<List<Map<String, String>>> getOperationModules() {
        List<Map<String, String>> modules = userLogService.getOperationModuleOptions();
        return ApiResult.success(modules);
    }
    
    /**
     * 导出日志数据
     */
    @PostMapping("/export")
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "导出日志数据", description = "根据查询条件导出日志数据为Excel或CSV文件")
    @OperationLog(moduleEnum = OperationModule.LOG, typeEnum = OperationType.EXPORT, description = "导出日志数据", recordResult = false, async = true)
    public ResponseEntity<byte[]> exportLogs(
            @RequestBody @Valid LogQueryRequest request,
            @Parameter(description = "导出类型：EXCEL, CSV") @RequestParam(defaultValue = "CSV") String exportType) {
        log.info("导出日志数据，参数: {}, 类型: {}", request, exportType);
        byte[] file = userLogService.exportLogs(request, exportType);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String suffix = "CSV".equalsIgnoreCase(exportType) ? ".csv" : ".csv";
        headers.setContentDispositionFormData("attachment", "user_logs_" + System.currentTimeMillis() + suffix);
        return ResponseEntity.ok().headers(headers).body(file);
    }
    
    /**
     * 清理过期日志（超级管理员权限）
     */
    @DeleteMapping("/expired")
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "清理过期日志", description = "删除指定时间之前的过期日志：普通用户仅删除本人，管理员删除本人及子账号，超级管理员删除全部")
    @OperationLog(moduleEnum = OperationModule.LOG, typeEnum = OperationType.CLEAN, description = "清理过期日志", async = true)
    public ApiResult<Integer> cleanExpiredLogs(
            @Parameter(description = "保留天数，删除此天数之前的日志") @RequestBody Map<String, Integer> body) {
        Integer retentionDays = body != null ? body.get("retentionDays") : null;
        if (retentionDays == null || retentionDays < 1 || retentionDays > 365) {
            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "保留天数必须在1-365之间");
        }
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
        log.info("清理过期日志，保留天数: {}，过期时间: {}", retentionDays, expireTime);
        int deletedCount = userLogService.cleanExpiredLogs(expireTime);
        return ApiResult.success("成功清理过期日志", deletedCount);
    }
    
    /**
     * 获取日志查询帮助信息
     */
    @GetMapping("/help")
    @SaCheckPermission("user-permission:log")
    @Operation(summary = "获取日志查询帮助信息", description = "获取日志查询功能的使用帮助和说明")
    public ApiResult<Object> getQueryHelp() {
        Map<String, Object> payload = new HashMap<String, Object>();

        // 已合并日类型，移除logTypes

        Map<String, String> quickTimeRanges = new HashMap<String, String>();
        quickTimeRanges.put("TODAY", "今天：查询今天的日志");
        quickTimeRanges.put("YESTERDAY", "昨天：查询昨天的日志");
        quickTimeRanges.put("WEEK", "近7天：查询最近7天的日志");
        quickTimeRanges.put("MONTH", "近30天：查询最近30天的日志");
        payload.put("quickTimeRanges", quickTimeRanges);

        List<String> searchTips = Arrays.asList(
            "用户名支持模糊搜索",
            "关键词会在操作描述和请求URL中搜索",
            "时间范围查询支持精确到秒",
            "支持按操作状态筛选成功/失败记录"
        );
        payload.put("searchTips", searchTips);

        return ApiResult.success("日志查询帮助", payload);
    }

    
} 
