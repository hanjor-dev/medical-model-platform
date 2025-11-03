/**
 * 引荐管理控制器：提供引荐码相关的REST API接口
 * 
 * 核心功能：
 * 1. 获取当前用户引荐码信息
 * 2. 验证引荐码有效性
 * 3. 生成引荐链接
 * 4. 集成权限控制和参数验证
 * 5. 遵循RESTful API设计规范
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 15:45:00
 */
package com.okbug.platform.controller.referral;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.referral.ReferralCodeResponse;
import com.okbug.platform.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;

import com.okbug.platform.dto.referral.ReferralUserResponse;

import java.util.List;

/**
 * 引荐管理控制器
 * 提供引荐码管理的所有REST API接口
 * 包括引荐码信息查询、验证、链接生成等核心功能
 */
@RestController
@RequestMapping("/referral")
@RequiredArgsConstructor
@Tag(name = "引荐管理接口")
@Validated
@Slf4j
public class ReferralController {
    private final ReferralService referralService;

    /**
     * 获取当前用户引荐码信息
     * 
     * 功能描述：
     * 1. 获取当前登录用户的引荐码信息
     * 2. 返回引荐码、引荐链接、分享描述等信息
     * 3. 需要用户登录状态
     * 4. 需要引荐码查询权限
     * 
     * @return 引荐码信息响应对象，包含引荐码、引荐链接、分享描述等信息
     */
    @GetMapping("/code")
    @Operation(summary = "获取当前用户引荐码信息", description = "返回用户的引荐码和引荐链接，需要用户登录")
    @SaCheckLogin
    public ApiResult<ReferralCodeResponse> getCurrentUserReferralCode() {
        log.info("获取当前用户引荐码信息请求");
        
        // 调用服务层获取引荐码信息
        ReferralCodeResponse response = referralService.getCurrentUserReferralCode();
        
        // 返回成功响应
        return ApiResult.success("获取引荐码信息成功", response);
    }
    

    
    /**
     * 获取引荐码基础链接配置
     * 
     * 功能描述：
     * 1. 获取系统配置的引荐码基础链接
     * 2. 可用于前端配置和调试
     * 3. 需要管理员权限
     * 
     * @return 引荐码基础链接配置
     */
    @GetMapping("/config/base-url")
    @Operation(summary = "获取引荐码基础链接配置", description = "获取系统配置的引荐码基础链接，需要管理员权限")
    @SaCheckPermission("system:config")
    public ApiResult<String> getReferralBaseUrl() {
        log.info("获取引荐码基础链接配置请求");
        
        // 调用服务层获取基础链接配置
        String baseUrl = referralService.getReferralBaseUrl();
        
        // 返回配置信息
        return ApiResult.success("获取基础链接配置成功", baseUrl);
    }
    
    /**
     * 获取当前用户的引荐用户列表
     * 
     * 功能描述：
     * 1. 获取当前登录用户通过引荐码注册的用户列表
     * 2. 支持分页查询
     * 3. 返回用户基本信息、注册时间、贡献积分等
     * 
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 引荐用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取当前用户的引荐用户列表", description = "获取通过引荐码注册的用户列表，支持分页查询")
    @SaCheckLogin
    public ApiResult<List<ReferralUserResponse>> getCurrentUserReferralUsers(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size) {
        
        log.info("获取当前用户引荐用户列表请求，页码: {}, 大小: {}", page, size);
        
        // 调用服务层获取引荐用户列表
        List<ReferralUserResponse> users = referralService.getCurrentUserReferralUsers(page, size);
        
        // 返回用户列表
        return ApiResult.success("获取引荐用户列表成功", users);
    }
    

} 
