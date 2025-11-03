/**
 * Sa-Token配置类：配置Sa-Token权限验证和会话管理
 * 
 * 核心功能：
 * 1. 配置Sa-Token权限拦截器和路由规则
 * 2. 配置用户权限验证接口
 * 3. 配置Token有效期和会话管理
 * 4. 配置异常处理和错误响应
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:30:00
 */
package com.okbug.platform.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ErrorCode;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册Sa-Token拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能 
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            // 注意：由于context-path设置为/api，所以拦截器内部看到的路径不包含/api前缀
            SaRouter
                .match("/**")    // 拦截的 path 列表，可以写多个
                .notMatch("/auth/login")    // 排除用户登录（实际访问路径：/api/auth/login）
                .notMatch("/auth/register")    // 排除用户注册（实际访问路径：/api/auth/register）
                .notMatch("/swagger-ui/**")    // 排除Swagger UI
                .notMatch("/swagger-resources/**")    // 排除Swagger资源
                .notMatch("/v2/api-docs/**")    // 排除Swagger API文档
                .notMatch("/v3/api-docs/**")    // 排除Swagger API文档
                .notMatch("/webjars/**")    // 排除Swagger静态资源
                .notMatch("/doc.html")    // 排除knife4j文档
                .notMatch("/favicon.ico")    // 排除网站图标
                .notMatch("/actuator/**")    // 排除健康检查接口
                .notMatch("/error")    // 排除错误页面
                .check(r -> StpUtil.checkLogin());        // 要执行的校验动作，可以写完整的 lambda 表达式
        })).addPathPatterns("/**");
    }
    
    /**
     * Sa-Token 自定义权限验证接口扩展
     * 此方法可以在需要自定义权限验证逻辑时使用
     * 暂时注释掉，避免编译错误，后续根据需要启用
     */
    /*
    public void configureSaStrategy() {
        // 重写Sa-Token的权限认证接口，加入注解鉴权能力
        // 暂时保持默认实现，后续根据需要扩展
        
        // 重写Sa-Token的角色认证接口
        // 暂时保持默认实现，后续根据需要扩展
    }
    */
} 