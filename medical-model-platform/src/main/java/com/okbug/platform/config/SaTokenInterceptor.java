/**
 * Sa-Token权限拦截器：实现接口权限和数据权限控制
 * 
 * 核心功能：
 * 1. Token有效性检查
 * 2. 接口权限验证
 * 3. 数据权限控制
 * 4. 权限异常处理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:15:00
 */
package com.okbug.platform.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaTokenInterceptor implements HandlerInterceptor {
    
    private final PermissionService permissionService;
    
    /**
     * 请求前置处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 获取请求路径
            String requestUri = request.getRequestURI();
            String method = request.getMethod();
            
            log.debug("权限拦截器处理请求: {} {}", method, requestUri);
            
            // 白名单路径直接放行（已在SaTokenConfig中配置）
            // 这里主要处理额外的权限验证逻辑
            
            // 检查登录状态
            if (StpUtil.isLogin()) {
                // 记录用户访问日志
                Long userId = StpUtil.getLoginIdAsLong();
                log.debug("用户访问接口，用户ID: {}, 接口: {} {}", userId, method, requestUri);
                
                // 可以在这里添加更多的权限验证逻辑
                // 例如：API调用频率限制、IP白名单检查等
            }
            
            return true;
            
        } catch (NotLoginException e) {
            // 未登录异常
            log.warn("用户未登录访问受保护接口: {}", request.getRequestURI());
            handleException(response, ErrorCode.UNAUTHORIZED, "请先登录");
            return false;
            
        } catch (NotRoleException e) {
            // 角色权限异常
            log.warn("用户角色权限不足，用户ID: {}, 需要角色: {}", StpUtil.getLoginIdDefaultNull(), e.getRole());
            handleException(response, ErrorCode.FORBIDDEN, "角色权限不足");
            return false;
            
        } catch (NotPermissionException e) {
            // 权限异常
            log.warn("用户权限不足，用户ID: {}, 需要权限: {}", StpUtil.getLoginIdDefaultNull(), e.getPermission());
            handleException(response, ErrorCode.FORBIDDEN, "权限不足");
            return false;
            
        } catch (Exception e) {
            // 其他异常
            log.error("权限拦截器处理异常", e);
            handleException(response, ErrorCode.INTERNAL_ERROR, "系统异常");
            return false;
        }
    }
    
    /**
     * 处理权限异常，返回JSON响应
     */
    private void handleException(HttpServletResponse response, ErrorCode errorCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResult<Object> result = ApiResult.error(errorCode, message);
        String jsonResponse = "{" +
                "\"code\":" + result.getCode() + "," +
                "\"message\":\"" + result.getMessage() + "\"," +
                "\"data\":null," +
                "\"timestamp\":" + result.getTimestamp() +
                "}";
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * 检查用户是否有访问指定用户数据的权限
     * 
     * @param targetUserId 目标用户ID
     * @return 是否有权限
     */
    public boolean checkDataPermission(Long targetUserId) {
        try {
            return permissionService.hasDataPermission(targetUserId);
        } catch (Exception e) {
            log.error("检查数据权限异常", e);
            return false;
        }
    }
    
    /**
     * 检查用户是否有指定权限
     * 
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    public boolean checkPermission(String permissionCode) {
        try {
            return permissionService.hasPermission(permissionCode);
        } catch (Exception e) {
            log.error("检查权限异常，权限编码: {}", permissionCode, e);
            return false;
        }
    }
    
    /**
     * 检查用户是否有指定角色
     * 
     * @param role 角色
     * @return 是否有角色
     */
    public boolean checkRole(String role) {
        try {
            return permissionService.hasRole(role);
        } catch (Exception e) {
            log.error("检查角色异常，角色: {}", role, e);
            return false;
        }
    }
} 