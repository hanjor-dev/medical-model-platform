/**
 * 权限管理控制器：提供权限查询相关的API接口
 * 
 * 核心接口：
 * 1. GET /api/permissions/user - 获取用户权限
 * 2. GET /api/permissions/menus - 获取菜单权限
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:50:00
 */
package com.okbug.platform.controller.permission;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.auth.response.MenuResponse;
import com.okbug.platform.dto.auth.response.UserPermissionResponse;
import com.okbug.platform.service.permission.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理接口")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 获取当前用户权限
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户权限", description = "获取当前用户的角色和权限列表")
    @SaCheckLogin
    public ApiResult<UserPermissionResponse> getUserPermissions() {
        log.info("获取用户权限请求");
        
        UserPermissionResponse response = permissionService.getCurrentUserPermissions();
        return ApiResult.success(response);
    }
    
    /**
     * 获取当前用户菜单权限
     */
    @GetMapping("/menus")
    @Operation(summary = "获取菜单权限", description = "根据用户权限返回可访问的菜单树")
    @SaCheckLogin
    public ApiResult<List<MenuResponse>> getUserMenus() {
        log.info("获取菜单权限请求");
        
        List<MenuResponse> menus = permissionService.getCurrentUserMenus();
        return ApiResult.success(menus);
    }
} 
