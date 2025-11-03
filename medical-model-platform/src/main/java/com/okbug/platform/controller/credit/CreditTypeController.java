/**
 * 积分类型管理控制器：提供积分类型的配置管理接口
 * 
 * 功能描述：
 * 1. 积分类型的CRUD操作
 * 2. 积分类型状态管理
 * 3. 积分类型排序管理
 * 4. 支持分页查询和搜索
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:15:00
 */
package com.okbug.platform.controller.credit;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.dto.credit.request.CreditTypeCreateRequest;
import com.okbug.platform.dto.credit.request.CreditTypeUpdateRequest;
import com.okbug.platform.dto.credit.response.CreditTypeResponse;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.service.credit.CreditTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/credits/types")
@RequiredArgsConstructor
@Validated
@Tag(name = "积分类型管理接口")
public class CreditTypeController {
    
    private final CreditTypeService creditTypeService;
    
    /**
     * 分页查询积分类型列表
     */
    @GetMapping
    @Operation(summary = "分页查询积分类型列表", description = "支持关键词搜索和状态筛选")
    public ApiResult<IPage<CreditTypeResponse>> getCreditTypePage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词", example = "VIP") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选", example = "1") @RequestParam(required = false) Integer status) {
        
        log.info("查询积分类型列表，页码: {}, 每页大小: {}, 关键词: {}, 状态: {}", pageNum, pageSize, keyword, status);
        
        Page<CreditType> page = new Page<>(pageNum, pageSize);
        IPage<CreditType> creditTypePage = creditTypeService.getCreditTypePage(page, keyword, status);
        
        // 转换为响应DTO
        IPage<CreditTypeResponse> responsePage = creditTypePage.convert(this::convertToResponse);
        
        return ApiResult.success(responsePage);
    }
    
    /**
     * 获取所有启用的积分类型
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的积分类型", description = "用于下拉选择等场景")
    @SaCheckLogin
    public ApiResult<List<CreditTypeResponse>> getEnabledCreditTypes() {
        log.info("查询启用的积分类型列表");
        
        List<CreditType> creditTypes = creditTypeService.getEnabledCreditTypes();
        List<CreditTypeResponse> responses = creditTypes.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 获取积分类型详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取积分类型详情", description = "根据ID获取积分类型详细信息")
    public ApiResult<CreditTypeResponse> getCreditTypeById(
            @Parameter(description = "积分类型ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("查询积分类型详情，ID: {}", id);
        
        CreditType creditType = creditTypeService.getCreditTypeById(id);
        
        CreditTypeResponse response = convertToResponse(creditType);
        return ApiResult.success(response);
    }
    
    /**
     * 创建积分类型
     */
    @PostMapping
    @Operation(summary = "创建积分类型", description = "创建新的积分类型配置")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_CREATE, description = "创建积分类型", async = true)
    public ApiResult<CreditTypeResponse> createCreditType(
            @Parameter(description = "创建请求", required = true) @Valid @RequestBody CreditTypeCreateRequest request) {
        
        log.info("创建积分类型，请求参数: {}", request);
        
        // 清理和标准化输入数据
        request.cleanAndNormalize();
        
        CreditType creditType = creditTypeService.createCreditType(request);
        CreditTypeResponse response = convertToResponse(creditType);
        
        return ApiResult.success("积分类型创建成功", response);
    }
    
    /**
     * 更新积分类型
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新积分类型", description = "更新积分类型信息")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_UPDATE, description = "更新积分类型", async = true)
    public ApiResult<CreditTypeResponse> updateCreditType(
            @Parameter(description = "积分类型ID", required = true) @PathVariable @NotNull Long id,
            @Parameter(description = "更新请求", required = true) @Valid @RequestBody CreditTypeUpdateRequest request) {
        
        log.info("更新积分类型，ID: {}, 请求参数: {}", id, request);
        
        // 清理和标准化输入数据
        request.cleanAndNormalize();
        
        CreditType creditType = creditTypeService.updateCreditType(id, request);
        CreditTypeResponse response = convertToResponse(creditType);
        
        return ApiResult.success("积分类型更新成功", response);
    }
    
    /**
     * 删除积分类型
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除积分类型", description = "软删除积分类型")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_DELETE, description = "删除积分类型", async = true)
    public ApiResult<Void> deleteCreditType(
            @Parameter(description = "积分类型ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("删除积分类型，ID: {}", id);
        
        boolean success = creditTypeService.deleteCreditType(id);
        if (!success) {
            return ApiResult.error("积分类型删除失败");
        }
        
        return ApiResult.success("积分类型删除成功");
    }
    
    /**
     * 启用积分类型
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用积分类型", description = "启用被禁用的积分类型")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_ENABLE, description = "启用积分类型", async = true)
    public ApiResult<Void> enableCreditType(
            @Parameter(description = "积分类型ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("启用积分类型，ID: {}", id);
        
        boolean success = creditTypeService.enableCreditType(id);
        if (!success) {
            return ApiResult.error("积分类型启用失败");
        }
        
        return ApiResult.success("积分类型启用成功");
    }
    
    /**
     * 禁用积分类型
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用积分类型", description = "禁用积分类型")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_DISABLE, description = "禁用积分类型", async = true)
    public ApiResult<Void> disableCreditType(
            @Parameter(description = "积分类型ID", required = true) @PathVariable @NotNull Long id) {
        
        log.info("禁用积分类型，ID: {}", id);
        
        boolean success = creditTypeService.disableCreditType(id);
        if (!success) {
            return ApiResult.error("积分类型禁用失败");
        }
        
        return ApiResult.success("积分类型禁用成功");
    }
    
    /**
     * 批量更新积分类型排序
     */
    @PutMapping("/sort")
    @Operation(summary = "批量更新积分类型排序", description = "调整积分类型的显示顺序")
    @SaCheckPermission("credit-system:type-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.TYPE_SORT_UPDATE, description = "批量更新积分类型排序", async = true)
    public ApiResult<Void> updateBatchSort(
            @Parameter(description = "排序列表", required = true) @RequestBody List<CreditType> creditTypes) {
        
        log.info("批量更新积分类型排序，数量: {}", creditTypes.size());
        
        int count = creditTypeService.updateBatchSort(creditTypes);
        
        return ApiResult.success("排序更新成功，更新数量: " + count);
    }
    
    // ================ 私有方法 ================
    
    /**
     * 将CreditType实体转换为CreditTypeResponse
     */
    private CreditTypeResponse convertToResponse(CreditType creditType) {
        if (creditType == null) {
            return null;
        }
        
        CreditTypeResponse response = new CreditTypeResponse();
        response.setId(creditType.getId());
        response.setTypeCode(creditType.getTypeCode());
        response.setTypeName(creditType.getTypeName());
        response.setDescription(creditType.getDescription());
        response.setUnitName(creditType.getUnitName());
        response.setIconUrl(creditType.getIconUrl());
        response.setColorCode(creditType.getColorCode());
        response.setDecimalPlaces(creditType.getDecimalPlaces());
        response.setIsTransferable(creditType.getIsTransferable() == 1);
        response.setStatus(creditType.getStatus());
        response.setSortOrder(creditType.getSortOrder());
        response.setCreateTime(creditType.getCreateTime());
        response.setUpdateTime(creditType.getUpdateTime());
        
        return response;
    }
} 
