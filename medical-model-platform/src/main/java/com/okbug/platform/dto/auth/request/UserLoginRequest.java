/**
 * 用户登录请求DTO：封装用户登录时的请求参数
 * 
 * 功能描述：
 * 1. 封装用户登录的必要参数
 * 2. 支持用户名和邮箱两种登录方式
 * 3. 提供参数验证注解
 * 4. 确保登录数据安全
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:45:00
 */
package com.okbug.platform.dto.auth.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {
    
    /**
     * 用户名或邮箱（必填）
     */
    @Schema(description = "用户名或邮箱", required = true, example = "zhang123")
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(max = 100, message = "用户名或邮箱长度不能超过100个字符")
    private String username;
    
    /**
     * 密码（必填）
     */
    @Schema(description = "密码", required = true, example = "abc123456")
    @NotBlank(message = "密码不能为空")
    @Size(max = 50, message = "密码长度不能超过50个字符")
    private String password;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理用户名/邮箱：去除首尾空格，转换为小写
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        
        // 密码不做处理，保持原样
    }
    
    /**
     * 判断登录标识是否为邮箱格式
     */
    public boolean isEmailLogin() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // 简单的邮箱格式检查
        return username.contains("@") && username.contains(".");
    }
    
    /**
     * 判断登录标识是否为用户名格式
     */
    public boolean isUsernameLogin() {
        return !isEmailLogin();
    }
} 
