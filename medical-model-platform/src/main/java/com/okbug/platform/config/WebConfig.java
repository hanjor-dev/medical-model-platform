/**
 * Web配置类：Web基础配置
 * 包含CORS跨域配置、请求编码配置等
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 15:30:00
 */
package com.okbug.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     * 支持前后端分离开发
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的域名，开发环境允许所有
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带认证信息
                .allowCredentials(true)
                // 预检请求有效期
                .maxAge(3600);
    }
} 