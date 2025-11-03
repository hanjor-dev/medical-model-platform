/**
 * 系统字典管理控制器：提供系统字典的完整REST API接口
 * 
 * 核心功能：
 * 1. 提供系统字典的完整CRUD操作
 * 2. 支持多级字典结构管理
 * 3. 支持字典状态控制和排序管理
 * 4. 提供字典映射关系和批量操作功能
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
import com.okbug.platform.dto.system.SystemDictDTO;
import com.okbug.platform.dto.system.SystemDictQueryDTO;
import com.okbug.platform.dto.system.SystemDictCreateDTO;
import com.okbug.platform.dto.system.SystemDictUpdateDTO;
import com.okbug.platform.dto.system.DictTreeDTO;
import com.okbug.platform.dto.system.DictModuleOptionDTO;
import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.dto.system.ImportResultDTO;
import com.okbug.platform.service.system.SystemDictService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 系统字典管理控制器
 * 提供系统字典管理的所有REST API接口
 * 包括基础CRUD、查询搜索、状态管理、排序管理、树形结构等核心功能
 */
@RestController
@RequestMapping("/system/dict")
@Tag(name = "系统字典管理")
@Validated
@Slf4j
public class SystemDictController {
    
    @Autowired
    private SystemDictService systemDictService;
    
    /**
     * 分页查询字典列表
     * 
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询字典列表")
    @SaCheckPermission("system:dict")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<SystemDictDTO>> getDicts(
            @Valid SystemDictQueryDTO queryDto) {
        log.info("分页查询字典列表，查询条件: {}", queryDto);
        
        try {
            com.baomidou.mybatisplus.core.metadata.IPage<SystemDictDTO> result = systemDictService.getDicts(queryDto);
            return ApiResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("分页查询字典列表失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取字典
     * 
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取字典")
    @SaCheckPermission("system:dict")
    public ApiResult<SystemDictDTO> getDictById(
            @Parameter(description = "字典ID") @PathVariable @NotNull(message = "字典ID不能为空") Long id) {
        log.info("根据ID获取字典，ID: {}", id);
        
        try {
            SystemDictDTO dict = systemDictService.getDictById(id);
            return ApiResult.success("查询成功", dict);
        } catch (Exception e) {
            log.error("根据ID获取字典失败，ID: {}", id, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新字典
     * 
     * @param createDto 创建请求
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建新字典")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_CREATE", description = "创建系统字典", async = true)
    public ApiResult<SystemDictDTO> createDict(
            @Parameter(description = "创建请求") @RequestBody @Valid SystemDictCreateDTO createDto) {
        log.info("创建新字典，字典编码: {}, 名称: {}", createDto.getDictCode(), createDto.getDictName());
        
        try {
            SystemDictDTO dict = systemDictService.createDict(createDto);
            return ApiResult.success("字典创建成功", dict);
        } catch (Exception e) {
            log.error("创建字典失败，字典编码: {}", createDto.getDictCode(), e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新字典
     * 
     * @param id 字典ID
     * @param updateDto 更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新字典")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_UPDATE", description = "更新系统字典", async = true)
    public ApiResult<SystemDictDTO> updateDict(
            @Parameter(description = "字典ID") @PathVariable @NotNull(message = "字典ID不能为空") Long id,
            @Parameter(description = "更新请求") @RequestBody @Valid SystemDictUpdateDTO updateDto) {
        log.info("更新字典，ID: {}, 更新内容: {}", id, updateDto);
        
        try {
            SystemDictDTO dict = systemDictService.updateDict(id, updateDto);
            return ApiResult.success("字典更新成功", dict);
        } catch (Exception e) {
            log.error("更新字典失败，ID: {}", id, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除字典
     * 
     * @param id 字典ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_DELETE", description = "删除系统字典", async = true)
    public ApiResult<Boolean> deleteDict(
            @Parameter(description = "字典ID") @PathVariable @NotNull(message = "字典ID不能为空") Long id) {
        log.info("删除字典，ID: {}", id);
        
        try {
            Boolean result = systemDictService.deleteDict(id);
            if (result) {
                return ApiResult.success("字典删除成功");
            } else {
                return ApiResult.error(ErrorCode.INTERNAL_ERROR, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除字典失败，ID: {}", id, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有模块列表（旧版本，返回字符串数组）
     * 
     * @return 模块列表
     * @deprecated 建议使用 /dict-modules 接口获取对象格式的模块列表
     */
    @GetMapping("/modules")
    @Operation(summary = "获取所有模块列表（旧版本）")
    @Deprecated
    @SaCheckPermission("system:dict")
    public ApiResult<List<String>> getModules() {
        log.debug("获取所有模块列表（旧版本）");
        
        try {
            List<String> modules = systemDictService.getModules();
            return ApiResult.success("查询成功", modules);
        } catch (Exception e) {
            log.error("获取模块列表失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取字典模块选项列表（用于前端下拉选择器）
     * 
     * @return 模块选项列表，格式：[{value: "SYSTEM", label: "系统模块"}, {value: "USER", label: "用户模块"}]
     */
    @GetMapping("/dict-modules")
    @Operation(summary = "获取字典模块选项列表")
    @SaCheckPermission("system:dict")
    public ApiResult<List<DictModuleOptionDTO>> getDictModules() {
        log.debug("获取字典模块选项列表");
        
        try {
            List<DictModuleOptionDTO> moduleOptions = systemDictService.getDictModules();
            return ApiResult.success("查询成功", moduleOptions);
        } catch (Exception e) {
            log.error("获取字典模块选项列表失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取字典树形结构（用于父级字典选择）
     * 
     * @param module 模块名称（可选）
     * @return 字典树形结构
     */
    @GetMapping("/tree-options")
    @Operation(summary = "获取字典树形结构选项")
    @SaCheckPermission("system:dict")
    public ApiResult<List<DictTreeDTO>> getDictTreeOptions(
            @Parameter(description = "模块名称（可选）") @RequestParam(required = false) String module) {
        log.debug("获取字典树形结构选项，模块: {}", module);
        
        try {
            List<DictTreeDTO> treeOptions = systemDictService.getDictTreeOptions(module);
            return ApiResult.success(treeOptions);
        } catch (Exception e) {
            log.error("获取字典树形结构选项失败，模块: {}", module, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统配置分类选项（来自字典 DICT_3 的子项）
     *
     * @return 分类选项列表
     */
    @GetMapping("/config-categories")
    @Operation(summary = "获取系统配置分类选项")
    @SaCheckPermission("system:dict")
    public ApiResult<List<DictDataDTO>> getConfigCategoryOptions() {
        log.debug("获取系统配置分类选项（DICT_1 子项）");
        try {
            List<DictDataDTO> options = systemDictService.getConfigCategoryOptions();
            return ApiResult.success("查询成功", options);
        } catch (Exception e) {
            log.error("获取系统配置分类选项失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统配置数据类型选项（来自字典 DICT_3 的子项）
     */
    @GetMapping("/config-types")
    @Operation(summary = "获取系统配置数据类型选项")
    @SaCheckPermission("system:dict")
    public ApiResult<List<DictDataDTO>> getConfigTypeOptions() {
        log.debug("获取系统配置数据类型选项（DICT_3 子项）");
        try {
            List<DictDataDTO> options = systemDictService.getConfigTypeOptions();
            return ApiResult.success("查询成功", options);
        } catch (Exception e) {
            log.error("获取系统配置数据类型选项失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新字典缓存
     * 
     * @return 操作结果
     */
    @PostMapping("/cache/refresh")
    @Operation(summary = "刷新字典缓存")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "CACHE_REFRESH", description = "刷新系统字典缓存", async = true)
    public ApiResult<Void> refreshDictCache() {
        log.info("刷新字典缓存");
        
        try {
            systemDictService.refreshDictCache();
            return ApiResult.success("字典缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新字典缓存失败", e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "刷新失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除字典缓存
     * 
     * @param dictCode 字典编码（可选）
     * @return 操作结果
     */
    @DeleteMapping("/cache")
    @Operation(summary = "清除字典缓存")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "CACHE_CLEAR", description = "清除系统字典缓存", async = true)
    public ApiResult<Void> clearDictCache(
            @Parameter(description = "字典编码（可选）") @RequestParam(required = false) String dictCode) {
        log.info("清除字典缓存，字典编码: {}", dictCode);
        
        try {
            systemDictService.clearDictCache(dictCode);
            return ApiResult.success("字典缓存清除成功");
        } catch (Exception e) {
            log.error("清除字典缓存失败，字典编码: {}", dictCode, e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "清除失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出选中字典
     * 
     * @param request 包含字典ID列表的请求体
     * @return Excel文件流
     */
    @PostMapping("/export")
    @Operation(summary = "导出选中字典")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_EXPORT", description = "导出系统字典", recordResult = false, async = true)
    public ResponseEntity<byte[]> exportSelectedDict(
            @Parameter(description = "字典ID列表") @RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        log.info("导出选中字典，ID列表: {}", ids);
        
        try {
            byte[] excelData = systemDictService.exportSelectedDict(ids);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "dict_export_" + System.currentTimeMillis() + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("导出选中字典失败，ID列表: {}", ids, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出全部字典
     * 
     * @param queryDto 查询条件
     * @return Excel文件流
     */
    @GetMapping("/export/all")
    @Operation(summary = "导出全部字典")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_EXPORT_ALL", description = "导出全部系统字典", recordResult = false, async = true)
    public ResponseEntity<byte[]> exportAllDict(@Valid SystemDictQueryDTO queryDto) {
        log.info("导出全部字典，查询条件: {}", queryDto);
        
        try {
            byte[] excelData = systemDictService.exportAllDict(queryDto);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "dict_export_all_" + System.currentTimeMillis() + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("导出全部字典失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导入字典数据
     * 
     * @param file 上传的Excel文件
     * @param overwrite 是否覆盖已存在的数据
     * @return 导入结果
     */
    @PostMapping("/import")
    @Operation(summary = "导入字典数据")
    @SaCheckPermission("system:dict")
    @OperationLog(module = "SYSTEM", type = "DICT_IMPORT", description = "导入系统字典数据", recordResult = false, async = true)
    public ApiResult<ImportResultDTO> importDict(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否覆盖已存在数据") @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        log.info("导入字典数据，文件名: {}, 覆盖模式: {}", file.getOriginalFilename(), overwrite);
        
        try {
            if (file.isEmpty()) {
                return ApiResult.error(ErrorCode.PARAM_INVALID, "上传文件不能为空");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".csv") && !fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ApiResult.error(ErrorCode.PARAM_INVALID, "文件格式不正确，请上传CSV或Excel文件");
            }
            
            ImportResultDTO result = systemDictService.importDict(
                    file.getBytes(), fileName, overwrite);
            
            return ApiResult.success("导入完成", result);
        } catch (Exception e) {
            log.error("导入字典数据失败，文件名: {}", file.getOriginalFilename(), e);
            return ApiResult.error(ErrorCode.INTERNAL_ERROR, "导入失败: " + e.getMessage());
        }
    }
}
