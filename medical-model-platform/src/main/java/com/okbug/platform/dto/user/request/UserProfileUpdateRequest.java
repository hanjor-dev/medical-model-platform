/**
 * 用户信息更新请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 13:00:00
 */
package com.okbug.platform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户信息更新请求")
public class UserProfileUpdateRequest {
    
    @Schema(description = "邮箱（可选）", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号（可选）", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "昵称", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @Schema(description = "头像URL（可选）", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;
} 
