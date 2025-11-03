/**
 * 积分使用场景管理控制器：提供积分使用场景的配置管理接口
 * 
 * 功能描述：
 * 1. 积分使用场景的CRUD操作
 * 2. 使用场景状态管理
 * 3. 使用场景规则配置
 * 4. 支持分页查询和搜索
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:30:00
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
import com.okbug.platform.dto.credit.request.CreditScenarioCreateRequest;
import com.okbug.platform.dto.credit.request.CreditScenarioUpdateRequest;
import com.okbug.platform.dto.credit.response.CreditScenarioResponse;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.service.credit.CreditScenarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.dto.credit.request.ScenarioExecuteRequest;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.service.credit.CreditService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/credits/scenarios")
@RequiredArgsConstructor
@Validated
@Tag(name = "积分使用场景管理接口")
public class CreditScenarioController {
    
    private final CreditScenarioService creditScenarioService;
    private final CreditService creditService;
    
    /**
     * 分页查询使用场景列表
     */
    @GetMapping
    @Operation(summary = "分页查询使用场景列表", description = "支持关键词搜索、积分类型筛选和状态筛选")
    @SaCheckLogin
    public ApiResult<IPage<CreditScenarioResponse>> getScenarioPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词", example = "AI计算") @RequestParam(required = false) String keyword,
            @Parameter(description = "积分类型编码", example = "NORMAL") @RequestParam(required = false) String creditTypeCode,
            @Parameter(description = "状态筛选", example = "1") @RequestParam(required = false) Integer status) {
        
        log.info("查询使用场景列表，页码: {}, 每页大小: {}, 关键词: {}, 积分类型: {}, 状态: {}", 
                pageNum, pageSize, keyword, creditTypeCode, status);
        
        Page<CreditUsageScenario> page = new Page<>(pageNum, pageSize);
        IPage<CreditUsageScenario> scenarioPage = creditScenarioService.getScenarioPage(page, keyword, creditTypeCode, status);
        
        // 转换为响应DTO
        IPage<CreditScenarioResponse> responsePage = scenarioPage.convert(this::convertToResponse);
        
        return ApiResult.success(responsePage);
    }
    
    /**
     * 获取所有启用的使用场景
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的使用场景", description = "用于下拉选择等场景")
    @SaCheckLogin
    public ApiResult<List<CreditScenarioResponse>> getEnabledScenarios() {
        log.info("查询启用的使用场景列表");
        
        List<CreditUsageScenario> scenarios = creditScenarioService.getEnabledScenarios();
        List<CreditScenarioResponse> responses = scenarios.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 根据积分类型获取使用场景
     */
    @GetMapping("/by-credit-type/{creditTypeCode}")
    @Operation(summary = "根据积分类型获取使用场景", description = "获取指定积分类型下的所有使用场景")
    @SaCheckLogin
    public ApiResult<List<CreditScenarioResponse>> getScenariosByCreditType(
            @Parameter(description = "积分类型编码", required = true) @PathVariable String creditTypeCode) {
        
        log.info("查询积分类型下的使用场景，积分类型: {}", creditTypeCode);
        
        List<CreditUsageScenario> scenarios = creditScenarioService.getScenariosByCreditType(creditTypeCode);
        List<CreditScenarioResponse> responses = scenarios.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 获取消费场景列表
     */
    @GetMapping("/consumption")
    @Operation(summary = "获取消费场景列表", description = "获取所有消耗积分的场景")
    @SaCheckLogin
    public ApiResult<List<CreditScenarioResponse>> getConsumptionScenarios() {
        log.info("查询消费场景列表");
        
        List<CreditUsageScenario> scenarios = creditScenarioService.getConsumptionScenarios();
        List<CreditScenarioResponse> responses = scenarios.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 获取奖励场景列表
     */
    @GetMapping("/reward")
    @Operation(summary = "获取奖励场景列表", description = "获取所有获得积分的场景")
    @SaCheckLogin
    public ApiResult<List<CreditScenarioResponse>> getRewardScenarios() {
        log.info("查询奖励场景列表");
        
        List<CreditUsageScenario> scenarios = creditScenarioService.getRewardScenarios();
        List<CreditScenarioResponse> responses = scenarios.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 根据用户角色获取可用场景
     */
    @GetMapping("/by-user-role/{userRole}")
    @Operation(summary = "根据用户角色获取可用场景", description = "获取指定用户角色可以使用的场景")
    @SaCheckLogin
    public ApiResult<List<CreditScenarioResponse>> getScenariosByUserRole(
            @Parameter(description = "用户角色", required = true) @PathVariable String userRole) {
        
        log.info("查询用户角色可用的使用场景，用户角色: {}", userRole);
        
        List<CreditUsageScenario> scenarios = creditScenarioService.getScenariosByUserRole(userRole);
        List<CreditScenarioResponse> responses = scenarios.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ApiResult.success(responses);
    }
    
    /**
     * 获取使用场景详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取使用场景详情", description = "根据ID获取使用场景详细信息")
    @SaCheckLogin
    public ApiResult<CreditScenarioResponse> getScenarioById(
            @Parameter(description = "使用场景ID", required = true) @PathVariable Long id) {
        
        log.info("查询使用场景详情，ID: {}", id);
        
        CreditUsageScenario scenario = creditScenarioService.getScenarioById(id);
        CreditScenarioResponse response = convertToResponse(scenario);
        
        return ApiResult.success(response);
    }
    
    /**
     * 根据场景编码获取使用场景
     */
    @GetMapping("/code/{scenarioCode}")
    @Operation(summary = "根据场景编码获取使用场景", description = "根据场景编码获取使用场景信息")
    @SaCheckLogin
    public ApiResult<CreditScenarioResponse> getScenarioByCode(
            @Parameter(description = "场景编码", required = true) @PathVariable String scenarioCode) {
        
        log.info("查询使用场景详情，场景编码: {}", scenarioCode);
        
        CreditUsageScenario scenario = creditScenarioService.getScenarioByCode(scenarioCode);
        CreditScenarioResponse response = convertToResponse(scenario);
        
        return ApiResult.success(response);
    }
    
    /**
     * 创建使用场景
     */
    @PostMapping
    @Operation(summary = "创建使用场景", description = "创建新的积分使用场景配置")
    @SaCheckPermission("credit-system:scenario-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_CREATE, description = "创建使用场景", async = true)
    public ApiResult<CreditScenarioResponse> createScenario(
            @Parameter(description = "创建请求", required = true) @Valid @RequestBody CreditScenarioCreateRequest request) {
        
        log.info("创建使用场景，请求参数: {}", request);
        
        // 清理和标准化输入数据
        request.cleanAndNormalize();
        
        CreditUsageScenario scenario = creditScenarioService.createScenario(request);
        CreditScenarioResponse response = convertToResponse(scenario);
        
        return ApiResult.success("使用场景创建成功", response);
    }
    
    /**
     * 更新使用场景
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新使用场景", description = "更新使用场景信息")
    @SaCheckPermission("credit-system:scenario-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_UPDATE, description = "更新使用场景", async = true)
    public ApiResult<CreditScenarioResponse> updateScenario(
            @Parameter(description = "使用场景ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新请求", required = true) @Valid @RequestBody CreditScenarioUpdateRequest request) {
        
        log.info("更新使用场景，ID: {}, 请求参数: {}", id, request);
        
        // 清理和标准化输入数据
        request.cleanAndNormalize();
        
        CreditUsageScenario scenario = creditScenarioService.updateScenario(id, request);
        CreditScenarioResponse response = convertToResponse(scenario);
        
        return ApiResult.success("使用场景更新成功", response);
    }
    
    /**
     * 删除使用场景
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除使用场景", description = "软删除使用场景")
    @SaCheckPermission("credit-system:scenario-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_DELETE, description = "删除使用场景", async = true)
    public ApiResult<Void> deleteScenario(
            @Parameter(description = "使用场景ID", required = true) @PathVariable Long id) {
        
        log.info("删除使用场景，ID: {}", id);
        
        boolean success = creditScenarioService.deleteScenario(id);
        if (!success) {
            return ApiResult.error("使用场景删除失败");
        }
        
        return ApiResult.success("使用场景删除成功");
    }
    
    /**
     * 启用使用场景
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用使用场景", description = "启用被禁用的使用场景")
    @SaCheckPermission("credit-system:scenario-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_ENABLE, description = "启用使用场景", async = true)
    public ApiResult<Void> enableScenario(
            @Parameter(description = "使用场景ID", required = true) @PathVariable Long id) {
        
        log.info("启用使用场景，ID: {}", id);
        
        boolean success = creditScenarioService.enableScenario(id);
        if (!success) {
            return ApiResult.error("使用场景启用失败");
        }
        
        return ApiResult.success("使用场景启用成功");
    }
    
    /**
     * 禁用使用场景
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用使用场景", description = "禁用使用场景")
    @SaCheckPermission("credit-system:scenario-management")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_DISABLE, description = "禁用使用场景", async = true)
    public ApiResult<Void> disableScenario(
            @Parameter(description = "使用场景ID", required = true) @PathVariable Long id) {
        
        log.info("禁用使用场景，ID: {}", id);
        
        boolean success = creditScenarioService.disableScenario(id);
        if (!success) {
            return ApiResult.error("使用场景禁用失败");
        }
        
        return ApiResult.success("使用场景禁用成功");
    }
    
    /**
     * 检查用户是否可以使用指定场景
     */
    @GetMapping("/check-permission/{scenarioCode}")
    @Operation(summary = "检查用户是否可以使用指定场景", description = "检查当前用户是否有权限使用指定场景")
    @SaCheckLogin
    public ApiResult<Object> checkUserPermission(
            @Parameter(description = "场景编码", required = true) @PathVariable String scenarioCode) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("检查用户使用场景权限，用户ID: {}, 场景编码: {}", currentUserId, scenarioCode);
        boolean allowed = creditService.canUserUseScenario(scenarioCode);
        return allowed ? ApiResult.success(true) : ApiResult.error("无权使用该场景");
    }
    
    /**
     * 检查用户是否超过每日使用限制
     */
    @GetMapping("/check-daily-limit/{scenarioCode}")
    @Operation(summary = "检查用户是否超过每日使用限制", description = "检查当前用户是否超过指定场景的每日使用限制")
    @SaCheckLogin
    public ApiResult<Object> checkDailyLimit(
            @Parameter(description = "场景编码", required = true) @PathVariable String scenarioCode) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("检查用户每日使用限制，用户ID: {}, 场景编码: {}", currentUserId, scenarioCode);
        boolean exceeded = creditService.isDailyLimitExceeded(scenarioCode);
        return ApiResult.success(java.util.Collections.singletonMap("exceeded", exceeded));
    }
    
    /**
     * 统一按场景执行积分操作（前置扣费/奖励），便于业务方只传场景码使用
     */
    @PostMapping("/execute")
    @Operation(summary = "按场景执行积分操作", description = "消费场景将前置扣费，奖励场景发放；建议传订单号用于幂等/退款")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.SCENARIO_EXECUTE, description = "按场景执行积分操作", async = true)
    public ApiResult<UserCredit> executeByScenario(@Valid @RequestBody ScenarioExecuteRequest request) {
        log.info("按场景执行积分操作，请求: {}", request);
        UserCredit result = creditService.applyScenario(request.getScenarioCode(), request.getRelatedOrderId(), request.getRelatedUserId(), request.getDescription());
        return ApiResult.success("执行成功", result);
    }

    // ================ 私有方法 ================
    
    /**
     * 将CreditUsageScenario实体转换为CreditScenarioResponse
     */
    private CreditScenarioResponse convertToResponse(CreditUsageScenario scenario) {
        if (scenario == null) {
            return null;
        }
        
        CreditScenarioResponse response = new CreditScenarioResponse();
        response.setId(scenario.getId());
        response.setScenarioCode(scenario.getScenarioCode());
        response.setScenarioName(scenario.getScenarioName());
        response.setCreditTypeCode(scenario.getCreditTypeCode());
        response.setCostPerUse(scenario.getCostPerUse());
        response.setDescription(scenario.getDescription());
        response.setDailyLimit(scenario.getDailyLimit());
        response.setUserRoles(scenario.getUserRoles());
        response.setStatus(scenario.getStatus());
        response.setCreateTime(scenario.getCreateTime());
        response.setUpdateTime(scenario.getUpdateTime());
        
        return response;
    }
} 
