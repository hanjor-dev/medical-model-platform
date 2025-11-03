package com.okbug.platform.controller.credit;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.credit.request.TransactionQueryRequest;
import com.okbug.platform.dto.common.OptionDTO;
import com.okbug.platform.common.enums.credit.CreditTransactionType;
import com.okbug.platform.dto.credit.response.TransactionResponse;
import com.okbug.platform.service.credit.CreditTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分交易记录控制器：提供交易记录的查询接口
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-08-27 15:50:00
 */
@Slf4j
@RestController
@RequestMapping("/credits/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "积分交易记录管理", description = "提供积分交易记录的查询功能")
public class CreditTransactionController {

    private final CreditTransactionService creditTransactionService;

    /**
     * 分页查询积分交易记录
     * 权限控制：
     * - 普通用户只能查询自己的记录
     * - 管理员可以查询自己和子账号的记录
     * - 超级管理员可以查询所有记录
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询积分交易记录", description = "支持多种筛选条件，包含权限控制")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<Page<TransactionResponse>> getTransactionPage(
            @Valid @RequestBody TransactionQueryRequest request) {
        log.info("分页查询积分交易记录，请求参数：{}", request);
        Page<TransactionResponse> result = creditTransactionService.getTransactionPage(request);
        return ApiResult.success("查询成功", result);
    }

    /**
     * 根据查询条件统计交易数据（总收入/总支出/净变动/笔数）
     */
    @PostMapping("/statistics")
    @Operation(summary = "根据查询条件统计交易数据", description = "返回总收入、总支出、净变动等统计信息")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<CreditTransactionService.TransactionStatistics> getTransactionStatistics(
            @Valid @RequestBody TransactionQueryRequest request) {
        log.info("根据查询条件统计交易数据，请求参数：{}", request);
        CreditTransactionService.TransactionStatistics stats = creditTransactionService.getTransactionStatistics(request);
        return ApiResult.success("查询成功", stats);
    }

    /**
     * 获取积分交易类型下拉选项
     */
    @GetMapping("/types")
    @Operation(summary = "获取交易类型选项", description = "返回交易类型的 value/label 列表")
    @SaCheckLogin
    public ApiResult<List<OptionDTO>> getTransactionTypeOptions() {
        log.info("获取积分交易类型下拉选项");
        List<OptionDTO> options = java.util.Arrays.stream(CreditTransactionType.values())
                .map(t -> new OptionDTO(t.getCode(), t.getDescription()))
                .collect(java.util.stream.Collectors.toList());
        return ApiResult.success(options);
    }

    /**
     * 查询当前用户的交易记录（不分页）
     */
    @GetMapping("/my")
    @Operation(summary = "查询当前用户的交易记录", description = "查询当前登录用户的所有交易记录")
    @SaCheckLogin
    public ApiResult<List<TransactionResponse>> getMyTransactions(
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "交易类型", example = "CONSUME") @RequestParam(required = false) String transactionType,
            @Parameter(description = "场景编码", example = "AI_COMPUTE") @RequestParam(required = false) String scenarioCode,
            @Parameter(description = "关键词", example = "AI计算") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTimeStr,
            @Parameter(description = "结束时间", example = "2025-12-31 23:59:59") @RequestParam(required = false) String endTimeStr) {
        
        log.info("查询当前用户交易记录，积分类型：{}，交易类型：{}，场景：{}，关键词：{}，时间范围：{} - {}", creditTypeCode, transactionType, scenarioCode, keyword, startTimeStr, endTimeStr);
        // 解析时间参数
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr.replace(" ", "T"));
        }
        
        if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr.replace(" ", "T"));
        }
        
        List<TransactionResponse> result = creditTransactionService.getTransactionsByUser(
            null, creditTypeCode, transactionType, scenarioCode, keyword, startTime, endTime);
        
        return ApiResult.success("查询成功", result);
    }

    /**
     * 查询指定用户的交易记录（管理员和超级管理员权限）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询指定用户的交易记录", description = "管理员和超级管理员可以查询指定用户的交易记录")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<List<TransactionResponse>> getUserTransactions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull @Min(1) Long userId,
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "交易类型", example = "CONSUME") @RequestParam(required = false) String transactionType,
            @Parameter(description = "场景编码", example = "AI_COMPUTE") @RequestParam(required = false) String scenarioCode,
            @Parameter(description = "关键词", example = "AI计算") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTimeStr,
            @Parameter(description = "结束时间", example = "2025-12-31 23:59:59") @RequestParam(required = false) String endTimeStr) {
        
        log.info("查询指定用户交易记录，用户ID：{}，积分类型：{}，交易类型：{}，场景：{}，关键词：{}，时间范围：{} - {}", 
                userId, creditTypeCode, transactionType, scenarioCode, keyword, startTimeStr, endTimeStr);
        // 解析时间参数
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr.replace(" ", "T"));
        }
        
        if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr.replace(" ", "T"));
        }
        
        List<TransactionResponse> result = creditTransactionService.getTransactionsByUser(
            userId, creditTypeCode, transactionType, scenarioCode, keyword, startTime, endTime);
        
        return ApiResult.success("查询成功", result);
    }

    /**
     * 查询交易记录详情
     */
    @GetMapping("/{transactionId}")
    @Operation(summary = "查询交易记录详情", description = "根据交易ID查询交易记录详情")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<TransactionResponse> getTransactionDetail(
            @Parameter(description = "交易ID", required = true) @PathVariable @NotNull @Min(1) Long transactionId) {
        
        log.info("查询交易记录详情，交易ID：{}", transactionId);
        TransactionResponse result = creditTransactionService.getTransactionById(transactionId);
        if (result == null) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_NOT_FOUND);
        }
        return ApiResult.success("查询成功", result);
    }

    /**
     * 查询关联交易记录
     */
    @GetMapping("/{transactionId}/related")
    @Operation(summary = "查询关联交易记录", description = "查询转账等双向交易的关联记录")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<List<TransactionResponse>> getRelatedTransactions(
            @Parameter(description = "交易ID", required = true) @PathVariable @NotNull @Min(1) Long transactionId) {
        
        log.info("查询关联交易记录，交易ID：{}", transactionId);
        List<TransactionResponse> result = creditTransactionService.getRelatedTransactions(transactionId);
        return ApiResult.success("查询成功", result);
    }

    /**
     * 查询当前用户的交易统计
     */
    @GetMapping("/my/statistics")
    @Operation(summary = "查询当前用户的交易统计", description = "查询当前用户的交易统计信息")
    @SaCheckLogin
    public ApiResult<CreditTransactionService.TransactionStatistics> getMyTransactionStatistics(
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTimeStr,
            @Parameter(description = "结束时间", example = "2025-12-31 23:59:59") @RequestParam(required = false) String endTimeStr) {
        
        log.info("查询当前用户交易统计，积分类型：{}，时间范围：{} - {}", creditTypeCode, startTimeStr, endTimeStr);
        // 解析时间参数
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr.replace(" ", "T"));
        }
        
        if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr.replace(" ", "T"));
        }
        
        CreditTransactionService.TransactionStatistics result = creditTransactionService.getTransactionStatistics(
            null, creditTypeCode, startTime, endTime);
        
        return ApiResult.success("查询成功", result);
    }

    /**
     * 查询指定用户的交易统计（管理员和超级管理员权限）
     */
    @GetMapping("/user/{userId}/statistics")
    @Operation(summary = "查询指定用户的交易统计", description = "管理员和超级管理员可以查询指定用户的交易统计")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<CreditTransactionService.TransactionStatistics> getUserTransactionStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull @Min(1) Long userId,
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTimeStr,
            @Parameter(description = "结束时间", example = "2025-12-31 23:59:59") @RequestParam(required = false) String endTimeStr) {
        
        log.info("查询指定用户交易统计，用户ID：{}，积分类型：{}，时间范围：{} - {}", 
                userId, creditTypeCode, startTimeStr, endTimeStr);
        // 解析时间参数
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr.replace(" ", "T"));
        }
        
        if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr.replace(" ", "T"));
        }
        
        CreditTransactionService.TransactionStatistics result = creditTransactionService.getTransactionStatistics(
            userId, creditTypeCode, startTime, endTime);
        
        return ApiResult.success("查询成功", result);
    }
} 
