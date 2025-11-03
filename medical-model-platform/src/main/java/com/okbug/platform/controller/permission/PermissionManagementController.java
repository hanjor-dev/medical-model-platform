/**
 * 权限管理控制器：提供动态权限配置的API接口
 * 
 * 核心接口：
 * 1. GET /api/permissions/tree - 获取权限树
 * 2. GET /api/permissions/role/{role}/tree - 获取角色权限树
 * 3. GET /api/permissions - 分页查询权限
 * 4. POST /api/permissions - 创建权限
 * 5. PUT /api/permissions/{id} - 更新权限
 * 6. DELETE /api/permissions/{id} - 删除权限
 * 7. PUT /api/permissions/role - 配置角色权限
 * 8. GET /api/permissions/user/{userId}/tree - 获取用户权限树
 * 9. GET /api/permissions/me/tree - 获取当前用户权限树
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:30:00
 */
package com.okbug.platform.controller.permission;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.permission.request.PermissionCreateRequest;
import com.okbug.platform.dto.permission.request.PermissionQueryRequest;
import com.okbug.platform.dto.permission.request.RolePermissionUpdateRequest;
import com.okbug.platform.dto.permission.request.UserPermissionOverrideUpdateRequest;
import com.okbug.platform.dto.permission.response.UserPermissionOverrideResponse;
import com.okbug.platform.dto.permission.response.PermissionTreeResponse;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.service.permission.PermissionManagementService;
import com.okbug.platform.service.permission.PermissionVersionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理接口")
public class PermissionManagementController {
    
    private final PermissionManagementService permissionManagementService;
    private final PermissionVersionService permissionVersionService;
    
    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "获取权限树", description = "获取完整的权限树形结构，仅超级管理员可访问")
    public ApiResult<List<PermissionTreeResponse>> getPermissionTree() {
        log.info("获取权限树形结构");
        List<PermissionTreeResponse> tree = permissionManagementService.getPermissionTree();
        return ApiResult.success("获取权限树成功", tree);
    }

    /**
     * 获取全局权限版本号（用于前端兜底比对）
     */
    @GetMapping("/version")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "获取权限版本", description = "返回全局权限版本号")
    public ApiResult<Long> getPermissionVersion() {
        long version = permissionVersionService.getGlobalVersion();
        return ApiResult.success(version);
    }
    
    /**
     * 获取角色权限树
     */
    @GetMapping("/role/{role}/tree")
    @Operation(summary = "获取角色权限树", description = "获取指定角色的权限树，标记已分配权限")
    public ApiResult<List<PermissionTreeResponse>> getRolePermissionTree(
            @Parameter(description = "角色", required = true, example = "ADMIN")
            @PathVariable String role) {
        log.info("获取角色权限树，角色: {}", role);
        List<PermissionTreeResponse> tree = permissionManagementService.getRolePermissionTree(role);
        return ApiResult.success("获取角色权限树成功", tree);
    }

    /**
     * 获取指定用户的权限树（节点含选中/禁用标记）
     */
    @GetMapping("/user/{userId}/tree")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "获取用户权限树", description = "获取指定用户的权限树，节点包含选中与禁用状态")
    public ApiResult<List<PermissionTreeResponse>> getUserPermissionTree(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        log.info("获取用户权限树，用户ID: {}", userId);
        List<PermissionTreeResponse> tree = permissionManagementService.getUserPermissionTree(userId);
        return ApiResult.success("获取用户权限树成功", tree);
    }
    
    /**
     * 分页查询权限
     */
    @GetMapping
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "分页查询权限", description = "支持按权限类型和关键词筛选")
    public ApiResult<IPage<Permission>> getPermissionPage(@Valid PermissionQueryRequest request) {
        log.info("分页查询权限，页码: {}, 大小: {}, 类型: {}, 关键词: {}", 
                request.getCurrent(), request.getSize(), request.getPermissionType(), request.getKeyword());
        
        IPage<Permission> result = permissionManagementService.getPermissionPage(request);
        return ApiResult.success("查询权限成功", result);
    }
    
    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
    public ApiResult<Permission> getPermissionById(
            @Parameter(description = "权限ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("获取权限详情，权限ID: {}", id);
        Permission permission = permissionManagementService.getPermissionById(id);
        return ApiResult.success("获取权限详情成功", permission);
    }
    
    /**
     * 创建权限
     */
    @PostMapping
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "创建权限", description = "创建新的权限项")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.CREATE, description = "创建权限", async = true)
    public ApiResult<Long> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        log.info("创建权限，权限编码: {}", request.getPermissionCode());
        Long permissionId = permissionManagementService.createPermission(request);
        return ApiResult.success("权限创建成功", permissionId);
    }
    
    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "更新权限", description = "更新指定权限的信息")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.UPDATE, description = "更新权限", async = true)
    public ApiResult<Void> updatePermission(
            @Parameter(description = "权限ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PermissionCreateRequest request) {
        log.info("更新权限，权限ID: {}, 权限编码: {}", id, request.getPermissionCode());
        permissionManagementService.updatePermission(id, request);
        return ApiResult.success("权限更新成功");
    }
    
    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "删除权限", description = "删除指定权限（不能有子权限）")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.DELETE, description = "删除权限", async = true)
    public ApiResult<Void> deletePermission(
            @Parameter(description = "权限ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("删除权限，权限ID: {}", id);
        permissionManagementService.deletePermission(id);
        return ApiResult.success("权限删除成功");
    }
    
    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "批量删除权限", description = "批量删除多个权限")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.BATCH_DELETE, description = "批量删除权限", async = true)
    public ApiResult<Void> batchDeletePermissions(
            @Parameter(description = "权限ID列表", required = true)
            @RequestBody List<Long> ids) {
        log.info("批量删除权限，数量: {}", ids.size());
        permissionManagementService.batchDeletePermissions(ids);
        return ApiResult.success("权限批量删除成功");
    }
    
    /**
     * 更新权限状态
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "更新权限状态", description = "启用或禁用权限")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.UPDATE, description = "更新权限状态", async = true)
    public ApiResult<Void> updatePermissionStatus(
            @Parameter(description = "权限ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "状态", required = true, example = "1")
            @RequestParam Integer status) {
        log.info("更新权限状态，权限ID: {}, 状态: {}", id, status);
        permissionManagementService.updatePermissionStatus(id, status);
        return ApiResult.success("权限状态更新成功");
    }
    
    /**
     * 配置角色权限
     */
    @PutMapping("/role")
    @Operation(summary = "配置角色权限", description = "为指定角色分配权限")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.UPDATE, description = "配置角色权限", async = true)
    public ApiResult<Void> updateRolePermissions(
            @Valid @RequestBody RolePermissionUpdateRequest request) {
        log.info("配置角色权限，角色: {}, 权限数量: {}", request.getRole(), 
                request.getPermissionIds() != null ? request.getPermissionIds().size() : 0);
        permissionManagementService.updateRolePermissions(request);
        return ApiResult.success("角色权限配置成功");
    }

    /**
     * 批量更新用户权限覆盖（允许/拒绝）
     */
    @PutMapping("/user/overrides")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "更新用户权限", description = "为指定用户批量设置允许的权限（ALLOW）")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.BATCH_UPDATE, description = "批量更新用户权限覆盖", async = true)
    public ApiResult<Void> updateUserPermissionOverrides(@Valid @RequestBody UserPermissionOverrideUpdateRequest request) {
        log.info("更新用户权限覆盖，用户ID: {}", request.getUserId());
        permissionManagementService.updateUserPermissionOverrides(request);
        return ApiResult.success("用户权限覆盖更新成功");
    }
    
    /**
     * 获取角色权限列表
     */
    @GetMapping("/role/{role}")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public ApiResult<List<Permission>> getRolePermissions(
            @Parameter(description = "角色", required = true, example = "ADMIN")
            @PathVariable String role) {
        log.info("获取角色权限列表，角色: {}", role);
        List<Permission> permissions = permissionManagementService.getRolePermissions(role);
        return ApiResult.success("获取角色权限成功", permissions);
    }
    
    /**
     * 刷新权限缓存
     */
    @PostMapping("/cache/refresh")
    @SaCheckPermission("user-permission:permission")
    @Operation(summary = "刷新权限缓存", description = "刷新所有用户的权限缓存")
    @OperationLog(moduleEnum = OperationModule.PERMISSION, typeEnum = OperationType.UPDATE, description = "刷新权限缓存", async = true)
    public ApiResult<Void> refreshPermissionCache() {
        log.info("刷新权限缓存");
        permissionManagementService.refreshAllPermissionCache();
        return ApiResult.success("权限缓存刷新成功");
    }

    /**
     * 获取指定用户的权限覆盖配置
     */
    @GetMapping("/user/{userId}/overrides")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "获取用户权限配置", description = "返回用户显式允许的权限ID列表，拒绝列表恒为空")
    public ApiResult<UserPermissionOverrideResponse> getUserPermissionOverrides(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        log.info("获取用户权限覆盖，用户ID: {}", userId);
        UserPermissionOverrideResponse resp = permissionManagementService.getUserPermissionOverrides(userId);
        return ApiResult.success("获取用户权限覆盖成功", resp);
    }

    /**
     * 获取指定用户的生效权限列表（完整权限对象）
     */
    @GetMapping("/user/{userId}/effective")
    @Operation(summary = "获取用户生效权限", description = "返回用户最终生效的权限列表")
    public ApiResult<List<Permission>> getUserEffectivePermissions(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        log.info("获取用户生效权限，用户ID: {}", userId);
        List<Permission> list = permissionManagementService.getUserEffectivePermissions(userId);
        return ApiResult.success("获取用户生效权限成功", list);
    }

    /**
     * 获取指定用户的生效权限编码
     */
    @GetMapping("/user/{userId}/effective/codes")
    @Operation(summary = "获取用户生效权限编码", description = "返回用户最终生效的权限编码列表")
    public ApiResult<List<String>> getUserEffectivePermissionCodes(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        log.info("获取用户生效权限编码，用户ID: {}", userId);
        List<String> codes = permissionManagementService.getUserEffectivePermissionCodes(userId);
        return ApiResult.success("获取用户生效权限编码成功", codes);
    }

    /**
     * 获取当前登录用户的生效权限列表
     */
    @GetMapping("/me/effective")
    @SaCheckLogin
    @Operation(summary = "获取本人生效权限", description = "返回当前登录用户的最终生效权限列表")
    public ApiResult<List<Permission>> getMyEffectivePermissions() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取本人生效权限，用户ID: {}", currentUserId);
        List<Permission> list = permissionManagementService.getUserEffectivePermissions(currentUserId);
        return ApiResult.success("获取本人生效权限成功", list);
    }

    /**
     * 获取当前登录用户的生效权限编码
     */
    @GetMapping("/me/effective/codes")
    @SaCheckLogin
    @Operation(summary = "获取本人生效权限编码", description = "返回当前登录用户的最终生效权限编码列表")
    public ApiResult<List<String>> getMyEffectivePermissionCodes() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取本人生效权限编码，用户ID: {}", currentUserId);
        List<String> codes = permissionManagementService.getUserEffectivePermissionCodes(currentUserId);
        return ApiResult.success("获取本人生效权限编码成功", codes);
    }

    /**
     * 获取当前登录用户的权限树（节点含选中/禁用标记）
     */
    @GetMapping("/me/tree")
    @SaCheckLogin
    @Operation(summary = "获取本人权限树", description = "获取当前登录用户的权限树，节点包含选中与禁用状态")
    public ApiResult<List<PermissionTreeResponse>> getMyPermissionTree() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取本人权限树，用户ID: {}", currentUserId);
        List<PermissionTreeResponse> tree = permissionManagementService.getUserPermissionTree(currentUserId);
        return ApiResult.success("获取本人权限树成功", tree);
    }
} 
