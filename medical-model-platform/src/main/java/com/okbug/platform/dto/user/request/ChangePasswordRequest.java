/**
 * 修改密码请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 13:05:00
 */
package com.okbug.platform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {
    
    @Schema(description = "旧密码", required = true, example = "oldPassword123")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @Schema(description = "新密码", required = true, example = "newPassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "新密码长度必须在8-20个字符之间")
    private String newPassword;
} 
