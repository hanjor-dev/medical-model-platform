/**
 * 系统配置管理控制器：提供系统配置的完整REST API接口
 * 
 * 核心功能：
 * 1. 提供系统配置的完整CRUD操作
 * 2. 支持配置分类管理和条件查询
 * 3. 支持配置状态控制和排序管理
 * 4. 提供配置值获取的便捷接口
 * 5. 集成权限控制和参数验证
 * 6. 遵循RESTful API设计规范
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.dto.system.SystemConfigDTO;
import com.okbug.platform.dto.system.SystemConfigQueryDTO;
import com.okbug.platform.dto.system.SystemConfigCreateDTO;
import com.okbug.platform.dto.system.SystemConfigUpdateDTO;
import com.okbug.platform.service.system.SystemConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供系统配置管理的所有REST API接口
 * 包括基础CRUD、查询搜索、状态管理、排序管理等核心功能
 */
@RestController
@RequestMapping("/system/configs")
@Tag(name = "系统配置管理")
@Validated
@Slf4j
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 分页查询配置列表
     * 
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询配置列表")
    @SaCheckPermission("system:config")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<SystemConfigDTO>> getConfigs(
            @Valid SystemConfigQueryDTO queryDto) {
        log.info("分页查询配置列表，查询条件: {}", queryDto);
        com.baomidou.mybatisplus.core.metadata.IPage<SystemConfigDTO> result = systemConfigService.getConfigs(queryDto);
        return ApiResult.success("查询成功", result);
    }
    
    /**
     * 根据ID获取配置
     * 
     * @param id 配置ID
     * @return 配置信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取配置")
    @SaCheckPermission("system:config")
    public ApiResult<SystemConfigDTO> getConfigById(
            @Parameter(description = "配置ID") @PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        log.info("根据ID获取配置，ID: {}", id);
        SystemConfigDTO config = systemConfigService.getConfigById(id);
        return ApiResult.success("查询成功", config);
    }
    
    /**
     * 根据配置键获取配置
     * 
     * @param configKey 配置键
     * @return 配置信息
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键获取配置")
    @SaCheckPermission("system:config")
    public ApiResult<SystemConfigDTO> getConfigByKey(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.info("根据配置键获取配置，配置键: {}", configKey);
        SystemConfigDTO config = systemConfigService.getConfigByKey(configKey);
        return ApiResult.success("查询成功", config);
    }
    
    /**
     * 创建新配置
     * 
     * @param createDto 创建请求
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建新配置")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_CREATE", description = "创建系统配置", async = true)
    public ApiResult<SystemConfigDTO> createConfig(
            @Parameter(description = "创建请求") @RequestBody @Valid SystemConfigCreateDTO createDto) {
        log.info("创建新配置，配置键: {}, 分类: {}", createDto.getConfigKey(), createDto.getConfigCategory());
        SystemConfigDTO config = systemConfigService.createConfig(createDto);
        return ApiResult.success("配置创建成功", config);
    }
    
    /**
     * 更新配置
     * 
     * @param id 配置ID
     * @param updateDto 更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新配置")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_UPDATE", description = "更新系统配置", async = true)
    public ApiResult<SystemConfigDTO> updateConfig(
            @Parameter(description = "配置ID") @PathVariable @NotNull(message = "配置ID不能为空") Long id,
            @Parameter(description = "更新请求") @RequestBody @Valid SystemConfigUpdateDTO updateDto) {
        log.info("更新配置，ID: {}, 新值: {}", id, updateDto.getConfigValue());
        SystemConfigDTO config = systemConfigService.updateConfig(id, updateDto);
        return ApiResult.success("配置更新成功", config);
    }
    
    /**
     * 删除配置
     * 
     * @param id 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_DELETE", description = "删除系统配置", async = true)
    public ApiResult<Boolean> deleteConfig(
            @Parameter(description = "配置ID") @PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        log.info("删除配置，ID: {}", id);
        Boolean result = systemConfigService.deleteConfig(id);
        if (result) {
            return ApiResult.success("配置删除成功");
        } else {
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "删除失败");
        }
    }
    
    /**
     * 批量删除配置
     * 
     * @param ids 配置ID列表
     * @return 删除数量
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除配置")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_BATCH_DELETE", description = "批量删除系统配置", async = true)
    public ApiResult<Integer> batchDeleteConfigs(
            @Parameter(description = "配置ID列表") @RequestBody @NotEmpty(message = "配置ID列表不能为空") List<Long> ids) {
        log.info("批量删除配置，ID数量: {}", ids.size());
        Integer deleteCount = systemConfigService.batchDelete(ids);
        return ApiResult.success("批量删除成功，删除数量: " + deleteCount, deleteCount);
    }
    
    /**
     * 批量更新配置状态
     * 
     * @param ids 配置ID列表
     * @param status 目标状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新配置状态")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_STATUS_UPDATE", description = "批量更新系统配置状态", async = true)
    public ApiResult<Integer> batchUpdateStatus(
            @Parameter(description = "配置ID列表") @RequestBody @NotEmpty(message = "配置ID列表不能为空") List<Long> ids,
            @Parameter(description = "目标状态") @RequestParam @NotNull(message = "目标状态不能为空") Integer status) {
        log.info("批量更新配置状态，配置ID: {}, 目标状态: {}", ids, status);
        Integer updateCount = systemConfigService.batchUpdateStatus(ids, status);
        return ApiResult.success("批量更新状态成功，更新数量: " + updateCount, updateCount);
    }
    
    /**
     * 批量更新配置排序
     * 
     * @param idSortMap 配置ID和排序值的映射关系
     * @return 更新结果
     */
    @PutMapping("/batch/sort")
    @Operation(summary = "批量更新配置排序")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CONFIG_SORT_UPDATE", description = "批量更新系统配置排序", async = true)
    public ApiResult<Integer> batchUpdateSortOrder(
            @Parameter(description = "配置ID和排序值映射") @RequestBody @NotEmpty(message = "排序映射不能为空") Map<Long, Integer> idSortMap) {
        log.info("批量更新配置排序，配置数量: {}", idSortMap.size());
        Integer updateCount = systemConfigService.batchUpdateSortOrder(idSortMap);
        return ApiResult.success("批量更新排序成功，更新数量: " + updateCount, updateCount);
    }
    
    /**
     * 根据配置键获取配置值（字符串）
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "根据配置键获取配置值（字符串）")
    @SaCheckPermission("system:config")
    public ApiResult<String> getConfigValue(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.debug("根据配置键获取配置值，配置键: {}", configKey);
        String value = systemConfigService.getConfigValue(configKey);
        return ApiResult.success("查询成功", value);
    }
    
    /**
     * 根据配置键获取配置值（字符串，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/default")
    @Operation(summary = "根据配置键获取配置值（带默认值）")
    @SaCheckPermission("system:config")
    public ApiResult<String> getConfigValueWithDefault(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey,
            @Parameter(description = "默认值") @RequestParam(required = false) String defaultValue) {
        log.debug("根据配置键获取配置值（带默认值），配置键: {}, 默认值: {}", configKey, defaultValue);
        
        try {
            String value = systemConfigService.getConfigValue(configKey, defaultValue);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置值（整数）
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/int")
    @Operation(summary = "根据配置键获取配置值（整数）")
    @SaCheckPermission("system:config")
    public ApiResult<Integer> getConfigValueAsInt(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.debug("根据配置键获取配置值（整数），配置键: {}", configKey);
        
        try {
            Integer value = systemConfigService.getConfigValueAsInt(configKey);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置值（整数，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/int/default")
    @Operation(summary = "根据配置键获取配置值（整数，带默认值）")
    @SaCheckPermission("system:config")
    public ApiResult<Integer> getConfigValueAsIntWithDefault(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey,
            @Parameter(description = "默认值") @RequestParam @NotNull(message = "默认值不能为空") Integer defaultValue) {
        log.debug("根据配置键获取配置值（整数，带默认值），配置键: {}, 默认值: {}", configKey, defaultValue);
        
        try {
            Integer value = systemConfigService.getConfigValueAsInt(configKey, defaultValue);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置值（布尔值）
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/boolean")
    @Operation(summary = "根据配置键获取配置值（布尔值）")
    @SaCheckPermission("system:config")
    public ApiResult<Boolean> getConfigValueAsBoolean(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.debug("根据配置键获取配置值（布尔值），配置键: {}", configKey);
        
        try {
            Boolean value = systemConfigService.getConfigValueAsBoolean(configKey);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置值（布尔值，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/boolean/default")
    @Operation(summary = "根据配置键获取配置值（布尔值，带默认值）")
    @SaCheckPermission("system:config")
    public ApiResult<Boolean> getConfigValueAsBooleanWithDefault(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey,
            @Parameter(description = "默认值") @RequestParam @NotNull(message = "默认值不能为空") Boolean defaultValue) {
        log.debug("根据配置键获取配置值（布尔值，带默认值），配置键: {}, 默认值: {}", configKey, defaultValue);
        
        try {
            Boolean value = systemConfigService.getConfigValueAsBoolean(configKey, defaultValue);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置值（JSON对象）
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/value/{configKey}/json")
    @Operation(summary = "根据配置键获取配置值（JSON对象）")
    @SaCheckPermission("system:config")
    public ApiResult<Object> getConfigValueAsJson(
            @Parameter(description = "配置键") @PathVariable @NotNull(message = "配置键不能为空") String configKey) {
        log.debug("根据配置键获取配置值（JSON对象），配置键: {}", configKey);
        
        try {
            Object value = systemConfigService.getConfigValueAsJson(configKey);
            return ApiResult.success("查询成功", value);
        } catch (Exception e) {
            log.error("根据配置键获取配置值失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有配置分类
     * 
     * @return 配置分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有配置分类")
    @SaCheckPermission("system:config")
    public ApiResult<List<String>> getConfigCategories() {
        log.debug("获取所有配置分类");
        
        try {
            List<String> categories = systemConfigService.getConfigCategories();
            return ApiResult.success("查询成功", categories);
        } catch (Exception e) {
            log.error("获取配置分类失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置分类获取配置列表
     * 
     * @param category 配置分类
     * @return 配置列表
     */
    @GetMapping("/categories/{category}")
    @Operation(summary = "根据配置分类获取配置列表")
    @SaCheckPermission("system:config")
    public ApiResult<List<SystemConfigDTO>> getConfigsByCategory(
            @Parameter(description = "配置分类") @PathVariable @NotNull(message = "配置分类不能为空") String category) {
        log.debug("根据配置分类获取配置列表，分类: {}", category);
        
        try {
            List<SystemConfigDTO> configs = systemConfigService.getConfigsByCategory(category);
            return ApiResult.success("查询成功", configs);
        } catch (Exception e) {
            log.error("根据配置分类获取配置列表失败，分类: {}", category, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据配置分类编码获取配置列表
     * 
     * @param categoryCode 配置分类编码（字典码）
     * @return 配置列表
     */
    @GetMapping("/categories/code/{categoryCode}")
    @Operation(summary = "根据配置分类编码获取配置列表")
    @SaCheckPermission("system:config")
    public ApiResult<List<SystemConfigDTO>> getConfigsByCategoryCode(
            @Parameter(description = "配置分类编码") @PathVariable @NotNull(message = "配置分类编码不能为空") String categoryCode) {
        log.debug("根据配置分类编码获取配置列表，分类编码: {}", categoryCode);
        
        try {
            List<SystemConfigDTO> configs = systemConfigService.getConfigsByCategoryCode(categoryCode);
            return ApiResult.success("查询成功", configs);
        } catch (Exception e) {
            log.error("根据配置分类编码获取配置列表失败，分类编码: {}", categoryCode, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新配置缓存
     * 
     * @return 操作结果
     */
    @PostMapping("/cache/refresh")
    @Operation(summary = "刷新配置缓存")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CACHE_REFRESH", description = "刷新系统配置缓存", async = true)
    public ApiResult<Void> refreshConfigCache() {
        log.info("刷新配置缓存");
        
        try {
            systemConfigService.refreshConfigCache();
            return ApiResult.success("配置缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "刷新失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除配置缓存
     * 
     * @param configKey 配置键（可选）
     * @return 操作结果
     */
    @DeleteMapping("/cache")
    @Operation(summary = "清除配置缓存")
    @SaCheckPermission("system:config")
    @OperationLog(module = "SYSTEM", type = "CACHE_CLEAR", description = "清除系统配置缓存", async = true)
    public ApiResult<Void> clearConfigCache(
            @Parameter(description = "配置键（可选）") @RequestParam(required = false) String configKey) {
        log.info("清除配置缓存，配置键: {}", configKey);
        
        try {
            systemConfigService.clearConfigCache(configKey);
            return ApiResult.success("配置缓存清除成功");
        } catch (Exception e) {
            log.error("清除配置缓存失败，配置键: {}", configKey, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "清除失败: " + e.getMessage());
        }
    }
} 
