/**
 * 用户注册请求DTO：封装用户注册时的请求参数
 * 
 * 功能描述：
 * 1. 封装用户注册的必要和可选参数
 * 2. 提供参数验证注解
 * 3. 支持推荐码机制
 * 4. 确保数据安全和完整性
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:40:00
 */
package com.okbug.platform.dto.auth.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest {
    
    /**
     * 用户名（必填，3-50位字符）
     */
    @Schema(description = "用户名", required = true, example = "zhang123")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码（必填，8-20位）
     */
    @Schema(description = "密码", required = true, example = "abc123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    private String password;
    
    /**
     * 昵称（可选，2-20位字符）
     */
    @Schema(description = "昵称", required = false, example = "张三")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;
    
    /**
     * 邮箱（可选）
     */
    @Schema(description = "邮箱", example = "zhang123@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号（可选，11位中国大陆手机号）
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 推荐码（可选，8位字符）
     */
    @Schema(description = "推荐码", example = "ABC12345")
    @Size(max = 20, message = "推荐码长度不能超过20个字符")
    private String referralCode;

    /**
     * 团队码（可选，20位以内）
     */
    @Schema(description = "团队码", example = "TEAMCODE123")
    @Size(max = 20, message = "团队码长度不能超过20个字符")
    private String teamCode;
    
    // ================ 便利方法 ================
    
    /**
     * 清理和标准化输入数据
     */
    public void cleanAndNormalize() {
        // 清理用户名：去除首尾空格，转换为小写
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        
        // 清理昵称：去除首尾空格
        if (nickname != null) {
            nickname = nickname.trim();
            if (nickname.isEmpty()) {
                nickname = null;
            }
        }
        
        // 清理邮箱：去除首尾空格，转换为小写
        if (email != null) {
            email = email.trim().toLowerCase();
            if (email.isEmpty()) {
                email = null;
            }
        }
        
        // 清理手机号：去除空格和特殊字符
        if (phone != null) {
            phone = phone.replaceAll("[\\s-()]", "");
            if (phone.isEmpty()) {
                phone = null;
            }
        }
        
        // 清理推荐码：去除空格
        if (referralCode != null) {
            referralCode = referralCode.trim();
            if (referralCode.isEmpty()) {
                referralCode = null;
            }
        }

        // 清理团队码：去除空格
        if (teamCode != null) {
            teamCode = teamCode.trim();
            if (teamCode.isEmpty()) {
                teamCode = null;
            }
        }
    }
    
    /**
     * 判断是否有推荐码
     */
    public boolean hasReferralCode() {
        return referralCode != null && !referralCode.trim().isEmpty();
    }

    /**
     * 判断是否有团队码
     */
    public boolean hasTeamCode() {
        return teamCode != null && !teamCode.trim().isEmpty();
    }
    
    /**
     * 判断是否有邮箱
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * 判断是否有手机号
     */
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }
    
    /**
     * 判断是否有昵称
     */
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }
} 
