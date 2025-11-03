/**
 * 积分管理控制器：提供积分查询、分配等管理功能
 * 
 * 功能描述：
 * 1. 用户积分余额查询
 * 2. 积分交易记录查询
 * 3. 管理员积分分配功能
 * 4. 积分统计和报表
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:35:00
 */
package com.okbug.platform.controller.credit;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.dto.credit.request.CreditAllocateRequest;
import com.okbug.platform.dto.credit.response.CreditBalanceResponse;
import com.okbug.platform.dto.credit.request.UserCreditsPageRequest;
import com.okbug.platform.dto.credit.response.UserCreditsSummaryResponse;
import com.okbug.platform.entity.credit.CreditTransaction;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.common.enums.credit.CreditTransactionType;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import com.okbug.platform.service.credit.CreditService;
import com.okbug.platform.service.credit.CreditTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
@Validated
@Tag(name = "积分管理接口")
public class CreditController {
    
    private final CreditService creditService;
    private final CreditTypeService creditTypeService;
    private final CreditTransactionMapper creditTransactionMapper;
    
    /**
     * 获取当前用户积分余额
     */
    @GetMapping("/balance")
    @Operation(summary = "获取当前用户积分余额", description = "返回用户所有积分类型的余额信息")
    @SaCheckLogin
    public ApiResult<CreditBalanceResponse> getCurrentUserBalance() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        log.info("查询当前用户积分余额，用户ID: {}", currentUserId);
        
        // 获取用户所有积分类型余额
        List<UserCredit> userCredits = creditService.getUserCreditBalanceList(null);
        
        // 构建响应对象
        CreditBalanceResponse response = new CreditBalanceResponse();
        response.setUserId(currentUserId);
        response.setQueryTime(LocalDateTime.now());
        
        List<CreditBalanceResponse.CreditAccountInfo> accounts = new ArrayList<>();
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (UserCredit userCredit : userCredits) {
            CreditBalanceResponse.CreditAccountInfo accountInfo = new CreditBalanceResponse.CreditAccountInfo();
            accountInfo.setCreditTypeCode(userCredit.getCreditTypeCode());
            accountInfo.setBalance(userCredit.getAvailableBalance());
            accountInfo.setTotalEarned(userCredit.getTotalEarnedAmount());
            accountInfo.setTotalConsumed(userCredit.getTotalConsumedAmount());
            
            // 获取积分类型信息
            CreditType creditType = creditTypeService.getCreditTypeByCode(userCredit.getCreditTypeCode());
            if (creditType != null) {
                accountInfo.setCreditTypeName(creditType.getTypeName());
                accountInfo.setUnitName(creditType.getUnitName());
                accountInfo.setIconUrl(creditType.getIconUrl());
                accountInfo.setColorCode(creditType.getColorCode());
                accountInfo.setIsTransferable(creditType.isTransferable());
                accountInfo.setDecimalPlaces(creditType.getDecimalPlaces());
            }
            
            accounts.add(accountInfo);
            totalBalance = totalBalance.add(userCredit.getAvailableBalance());
        }
        
        response.setAccounts(accounts);
        response.setTotalBalance(totalBalance);
        response.setAccountCount(accounts.size());
        
        return ApiResult.success(response);
    }
    
    /**
     * 获取指定用户积分余额（管理员功能）
     */
    @GetMapping("/admin/balance/{userId}")
    @Operation(summary = "获取指定用户积分余额", description = "管理员查询指定用户的积分余额")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<CreditBalanceResponse> getUserBalance(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        
        log.info("管理员查询用户积分余额，用户ID: {}", userId);
        
        // 获取指定用户所有积分类型余额（含数据域校验）
        List<UserCredit> userCredits = creditService.getUserCreditBalanceListByUserId(userId);
        
        // 构建响应对象
        CreditBalanceResponse response = new CreditBalanceResponse();
        response.setUserId(userId);
        response.setQueryTime(LocalDateTime.now());
        
        List<CreditBalanceResponse.CreditAccountInfo> accounts = new ArrayList<>();
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (UserCredit userCredit : userCredits) {
            CreditBalanceResponse.CreditAccountInfo accountInfo = new CreditBalanceResponse.CreditAccountInfo();
            accountInfo.setCreditTypeCode(userCredit.getCreditTypeCode());
            accountInfo.setBalance(userCredit.getAvailableBalance());
            accountInfo.setTotalEarned(userCredit.getTotalEarnedAmount());
            accountInfo.setTotalConsumed(userCredit.getTotalConsumedAmount());
            
            // 获取积分类型信息
            CreditType creditType = creditTypeService.getCreditTypeByCode(userCredit.getCreditTypeCode());
            if (creditType != null) {
                accountInfo.setCreditTypeName(creditType.getTypeName());
                accountInfo.setUnitName(creditType.getUnitName());
                accountInfo.setIconUrl(creditType.getIconUrl());
                accountInfo.setColorCode(creditType.getColorCode());
                accountInfo.setIsTransferable(creditType.isTransferable());
                accountInfo.setDecimalPlaces(creditType.getDecimalPlaces());
            }
            
            accounts.add(accountInfo);
            totalBalance = totalBalance.add(userCredit.getAvailableBalance());
        }
        
        response.setAccounts(accounts);
        response.setTotalBalance(totalBalance);
        response.setAccountCount(accounts.size());
        
        return ApiResult.success(response);
    }
    
    /**
     * 获取当前用户积分交易记录
     */
    @GetMapping("/transactions")
    @Operation(summary = "获取当前用户积分交易记录", description = "支持分页查询和条件筛选")
    @SaCheckLogin
    public ApiResult<IPage<CreditTransaction>> getCurrentUserTransactions(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "交易类型", example = "CONSUME") @RequestParam(required = false) String transactionType,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-01-15 23:59:59") @RequestParam(required = false) String endTime) {
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        log.info("查询当前用户积分交易记录，用户ID: {}, 页码: {}, 每页大小: {}", currentUserId, pageNum, pageSize);
        
        // 构建分页对象
        Page<CreditTransaction> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditTransaction::getUserId, currentUserId);
        
        if (StringUtils.hasText(creditTypeCode)) {
            queryWrapper.eq(CreditTransaction::getCreditTypeCode, creditTypeCode);
        }
        
        if (StringUtils.hasText(transactionType)) {
            queryWrapper.eq(CreditTransaction::getTransactionType, transactionType);
        }
        
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge(CreditTransaction::getCreateTime, start);
        }
        
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le(CreditTransaction::getCreateTime, end);
        }
        
        queryWrapper.orderByDesc(CreditTransaction::getCreateTime);
        
        // 执行查询
        IPage<CreditTransaction> result = creditTransactionMapper.selectPage(page, queryWrapper);
        
        return ApiResult.success(result);
    }

    /**
     * 管理员向用户分配积分
     */
    @PostMapping("/admin/allocate")
    @Operation(summary = "管理员向用户分配积分", description = "管理员向指定用户分配指定类型的积分")
    @SaCheckPermission("credit-system:user-credits")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.ALLOCATE, description = "管理员分配积分", async = true)
    public ApiResult<Object> allocateCredits(@Valid @RequestBody CreditAllocateRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        log.info("管理员分配积分，管理员ID: {}, 目标用户ID: {}, 积分类型: {}, 积分数: {}", 
                currentUserId, request.getTargetUserId(), request.getCreditTypeCode(), request.getAmount());
        
        // 执行积分分配（权限与参数校验由Service抛出统一异常）
        UserCredit userCredit = creditService.allocateCredits(
            request.getTargetUserId(), 
            request.getCreditTypeCode(), 
            request.getAmount(), 
            request.getDescription()
        );
        
        return ApiResult.success("积分分配成功", userCredit);
    }
    
    /**
     * 查询积分分配历史
     */
    @GetMapping("/admin/allocation-history")
    @Operation(summary = "查询积分分配历史", description = "管理员查询积分分配历史记录")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<IPage<CreditTransaction>> getAllocationHistory(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "目标用户ID", example = "123") @RequestParam(required = false) Long targetUserId,
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-01-15 23:59:59") @RequestParam(required = false) String endTime) {
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        log.info("查询积分分配历史，管理员ID: {}, 页码: {}, 每页大小: {}", currentUserId, pageNum, pageSize);
        
        // 构建分页对象
        Page<CreditTransaction> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditTransaction::getTransactionType, CreditTransactionType.ADMIN.getCode());
        
        if (targetUserId != null) {
            queryWrapper.eq(CreditTransaction::getUserId, targetUserId);
        }
        
        if (StringUtils.hasText(creditTypeCode)) {
            queryWrapper.eq(CreditTransaction::getCreditTypeCode, creditTypeCode);
        }
        
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge(CreditTransaction::getCreateTime, start);
        }
        
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le(CreditTransaction::getCreateTime, end);
        }
        
        queryWrapper.orderByDesc(CreditTransaction::getCreateTime);
        
        // 执行查询
        IPage<CreditTransaction> result = creditTransactionMapper.selectPage(page, queryWrapper);
        
        return ApiResult.success(result);
    }
    
    /**
     * 获取积分统计信息
     */
    @GetMapping("/admin/statistics")
    @Operation(summary = "获取积分统计信息", description = "管理员查看积分系统统计信息")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<Object> getCreditStatistics(
            @Parameter(description = "统计类型", example = "DAILY") @RequestParam(defaultValue = "DAILY") String type,
            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-01-15 23:59:59") @RequestParam(required = false) String endTime) {
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        log.info("获取积分统计信息，管理员ID: {}, 统计类型: {}", currentUserId, type);
        
        // TODO: 实现积分统计逻辑
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("type", type);
        statistics.put("message", "积分统计功能暂未完全实现，请等待后续版本");
        
        return ApiResult.success(statistics);
    }
    
    // ================ 私有辅助方法 ================
    
    /**
     * 检查管理员权限
     */
    // 移除硬编码的管理员校验逻辑，统一由权限注解与Service层处理

    /**
     * 分页查询用户 + 积分账户汇总（管理员）
     */
    @PostMapping("/admin/users/balances/page")
    @Operation(summary = "分页查询用户积分汇总", description = "管理员分页查询用户及其积分账户汇总")
    @SaCheckPermission("credit-system:user-credits")
    public ApiResult<IPage<UserCreditsSummaryResponse>> getUserCreditsSummaryPage(@Valid @RequestBody UserCreditsPageRequest request) {
        Page<com.okbug.platform.entity.auth.User> page = request.toPage();
        IPage<UserCreditsSummaryResponse> result = creditService.getUserCreditsSummaryPage(page, request.getKeyword());
        return ApiResult.success(result);
    }
} 
