/**
 * 认证服务接口：定义用户认证相关的业务操作
 * 
 * 功能描述：
 * 1. 用户注册业务逻辑
 * 2. 用户登录验证和Token生成
 * 3. 用户退出登录处理
 * 4. Token刷新机制
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:05:00
 */
package com.okbug.platform.service.auth;

import com.okbug.platform.dto.auth.request.UserLoginRequest;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserLoginResponse;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;

public interface AuthService {
    
    /**
     * 用户注册
     * 
     * @param request 注册请求参数
     * @return 注册响应信息
     */
    UserRegisterResponse register(UserRegisterRequest request);
    
    /**
     * 用户登录
     * 
     * @param request 登录请求参数
     * @return 登录响应信息（包含Token和用户信息）
     */
    UserLoginResponse login(UserLoginRequest request);
    
    /**
     * 用户退出登录
     * 当前用户退出登录，清除Sa-Token会话
     */
    void logout();
    
    /**
     * 刷新Token
     * 刷新当前用户的Sa-Token有效期
     * 
     * @return 新的Token
     */
    String refreshToken();

    /**
     * 获取当前登录会话的用户信息（含权限、当前团队）
     */
    UserLoginResponse getSessionUser();
} 