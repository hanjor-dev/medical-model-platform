/**
 * 认证控制器：提供用户认证相关的API接口
 * 
 * 核心接口：
 * 1. POST /api/auth/register - 用户注册
 * 2. POST /api/auth/login - 用户登录
 * 3. POST /api/auth/logout - 用户退出
 * 4. POST /api/auth/refresh - Token刷新
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:20:00
 */
package com.okbug.platform.controller.auth;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.auth.request.UserLoginRequest;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserLoginResponse;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;
import com.okbug.platform.service.auth.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证接口")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，支持推荐码机制")
    @OperationLog(module = "AUTH", type = "REGISTER", description = "用户注册", recordParams = true, async = true)
    public ApiResult<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求，用户名: {}", request.getUsername());
        
        UserRegisterResponse response = authService.register(request);
        return ApiResult.success("注册成功", response);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名/邮箱+密码登录，返回Token、用户信息和完整权限对象列表（用于前端动态生成菜单）")
    @OperationLog(module = "AUTH", type = "LOGIN", description = "用户登录", recordParams = true, async = true)
    public ApiResult<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("用户登录请求，用户名: {}", request.getUsername());
        
        UserLoginResponse response = authService.login(request);
        return ApiResult.success("登录成功", response);
    }
    
    /**
     * 用户退出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "退出登录，清除Token")
    @SaCheckLogin
    @OperationLog(module = "AUTH", type = "LOGOUT", description = "用户退出登录", async = true)
    public ApiResult<Void> logout() {
        log.info("用户退出登录请求");
        
        authService.logout();
        return ApiResult.success("退出成功");
    }
    
    /**
     * Token刷新
     */
    @PostMapping("/refresh")
    @Operation(summary = "Token刷新", description = "刷新Token有效期")
    @SaCheckLogin
    public ApiResult<String> refresh() {
        log.info("Token刷新请求");
        
        String newToken = authService.refreshToken();
        return ApiResult.success("刷新成功", newToken);
    }
    
    /**
     * 获取会话用户信息（含权限和当前团队）
     */
    @GetMapping("/session")
    @Operation(summary = "获取会话用户信息", description = "返回当前登录用户的基本信息、权限、当前团队")
    @SaCheckLogin
    public ApiResult<UserLoginResponse> session() {
        UserLoginResponse response = authService.getSessionUser();
        return ApiResult.success(response);
    }
} 
