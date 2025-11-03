/**
 * 用户管理控制器：提供用户管理相关的API接口
 * 
 * 核心接口：
 * 1. GET /api/user/profile - 获取用户信息
 * 2. PUT /api/user/profile - 修改用户信息
 * 3. PUT /api/user/password - 修改密码
 * 4. GET /api/user/children - 获取子账号列表
 * 5. POST /api/user/children - 创建子账号
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:45:00
 */
package com.okbug.platform.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.okbug.platform.common.annotation.OperationLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;
import com.okbug.platform.dto.user.request.ChangePasswordRequest;
import com.okbug.platform.dto.user.request.UserProfileUpdateRequest;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理接口")
public class UserController {
    
    private final UserService userService;
    private final SystemConfigService systemConfigService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @SaCheckLogin
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResult<User> getUserProfile() {
        log.info("获取用户信息请求");
        
        User user = userService.getCurrentUserProfile();
        return ApiResult.success(user);
    }
    
    /**
     * 修改当前用户信息
     */
    @PutMapping("/profile")
    @SaCheckLogin
    @Operation(summary = "修改用户信息", description = "修改当前用户的个人信息")
    @OperationLog(module = "USER", type = "UPDATE", description = "修改用户信息", recordParams = false, async = true)
    public ApiResult<User> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("修改用户信息请求，邮箱: {}, 手机号: {}, 昵称: {}", 
                request.getEmail(), request.getPhone(), request.getNickname());
        User updatedUser = userService.updateCurrentUserProfile(request);
        return ApiResult.success("修改成功", updatedUser);
    }
    
    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    @SaCheckLogin
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @OperationLog(module = "USER", type = "PASSWORD_CHANGE", description = "修改密码", recordParams = false, async = true)
    public ApiResult<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求");
        
        userService.changePassword(request);
        return ApiResult.success("密码修改成功");
    }
    
    private void ensureSubAccountApiEnabled() {
        boolean enabled = Boolean.parseBoolean(systemConfigService.getConfigValue(
                SystemConfigKeys.SUBACCOUNT_API_ENABLED,
                SystemConfigKeys.getDefaultValue(SystemConfigKeys.SUBACCOUNT_API_ENABLED)));
        if (!enabled) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "子账号API已下线，请使用团队成员功能");
        }
    }
    
    /**
     * 获取子账号列表（ADMIN权限）
     */
    @GetMapping("/children")
    @cn.dev33.satoken.annotation.SaCheckRole("SUPER_ADMIN")
    @Operation(summary = "获取用户列表", description = "仅超级管理员可用，返回全量用户（排除自己）")
    public ApiResult<IPage<User>> getSubUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "团队ID（可选）") @RequestParam(required = false) Long teamId) {
        log.info("获取用户列表请求(超级管理员)，页码: {}, 大小: {}", page, size);
        
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> result = userService.getSubUsers(pageParam, teamId);
        return ApiResult.success(result);
    }
    
    /**
     * 创建子账号（ADMIN权限）
     */
    @PostMapping("/children")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "创建子账号", description = "管理员为自己创建子账号")
    @OperationLog(module = "USER", type = "CREATE", description = "创建子账号", recordParams = false, async = true)
    public ApiResult<UserRegisterResponse> createSubUser(@Valid @RequestBody UserRegisterRequest request) {
        ensureSubAccountApiEnabled();
        log.info("创建子账号请求，用户名: {}", request.getUsername());
        
        UserRegisterResponse response = userService.createSubUser(request);
        return ApiResult.success("创建成功", response);
    }

    /**
     * 更新子账号信息（ADMIN权限）
     */
    @PutMapping("/children/{userId}")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "更新子账号信息", description = "管理员更新自己管理的子账号信息")
    @OperationLog(module = "USER", type = "UPDATE", description = "更新子账号信息", recordParams = false, async = true)
    public ApiResult<User> updateSubUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        ensureSubAccountApiEnabled();
        log.info("更新子账号信息，请求用户ID: {}", userId);
        User updated = userService.updateSubUser(userId, request);
        return ApiResult.success("更新成功", updated);
    }

    /**
     * 删除子账号（ADMIN权限）
     */
    @DeleteMapping("/children/{userId}")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "删除子账号", description = "管理员删除自己管理的子账号")
    @OperationLog(module = "USER", type = "DELETE", description = "删除子账号", async = true)
    public ApiResult<Void> deleteSubUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        ensureSubAccountApiEnabled();
        log.info("删除子账号，请求用户ID: {}", userId);
        userService.deleteSubUser(userId);
        return ApiResult.success("删除成功");
    }

    /**
     * 切换子账号状态（ADMIN权限）
     */
    @PutMapping("/children/{userId}/status")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "切换子账号状态", description = "管理员启用/禁用自己管理的子账号")
    @OperationLog(module = "USER", type = "STATUS_CHANGE", description = "切换子账号状态", async = true)
    public ApiResult<Void> toggleSubUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "状态(0禁用/1启用)") @RequestParam Integer status) {
        ensureSubAccountApiEnabled();
        log.info("切换子账号状态，请求用户ID: {}, 状态: {}", userId, status);
        userService.toggleSubUserStatus(userId, status);
        return ApiResult.success("状态更新成功");
    }

    /**
     * 重置子账号密码（ADMIN权限）
     */
    @PutMapping("/children/{userId}/password")
    @SaCheckPermission("user-permission:user")
    @Operation(summary = "重置子账号密码", description = "管理员为自己管理的子账号重置密码")
    @OperationLog(module = "USER", type = "PASSWORD_RESET", description = "重置子账号密码", recordParams = false, async = true)
    public ApiResult<String> resetSubUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        ensureSubAccountApiEnabled();
        log.info("重置子账号密码，请求用户ID: {}", userId);
        String defaultPassword = userService.resetSubUserPassword(userId);
        return ApiResult.success("密码已重置", defaultPassword);
    }
} 
