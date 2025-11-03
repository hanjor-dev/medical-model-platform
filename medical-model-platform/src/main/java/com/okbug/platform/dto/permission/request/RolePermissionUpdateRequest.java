/**
 * 角色权限配置请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:05:00
 */
package com.okbug.platform.dto.permission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "角色权限配置请求")
public class RolePermissionUpdateRequest {
    
    @Schema(description = "角色", required = true, example = "ADMIN")
    @NotBlank(message = "角色不能为空")
    private String role;
    
    @Schema(description = "权限ID列表", required = true, example = "[1,2,3,4,5]")
    @NotNull(message = "权限列表不能为空")
    private List<Long> permissionIds;
} 
