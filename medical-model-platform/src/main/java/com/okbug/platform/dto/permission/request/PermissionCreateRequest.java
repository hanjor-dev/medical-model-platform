/**
 * 权限创建请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:00:00
 */
package com.okbug.platform.dto.permission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Schema(description = "权限创建请求")
public class PermissionCreateRequest {
    
    @Schema(description = "权限编码", required = true, example = "system:user:add")
    @NotBlank(message = "权限编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_:]*$", message = "权限编码格式不正确，只能包含字母、数字、下划线和冒号")
    private String permissionCode;
    
    @Schema(description = "权限名称", required = true, example = "新增用户")
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;
    
    @Schema(description = "权限类型", required = true, example = "MENU")
    @NotBlank(message = "权限类型不能为空")
    @Pattern(regexp = "^(MENU|BUTTON|API)$", message = "权限类型只能是MENU、BUTTON、API之一")
    private String permissionType;
    
    @Schema(description = "父权限ID", example = "1")
    private Long parentId;
    
    @Schema(description = "菜单路径", example = "/system/user")
    private String menuPath;
    
    @Schema(description = "组件路径", example = "system/user/index")
    private String componentPath;
    
    @Schema(description = "菜单图标", example = "user")
    private String menuIcon;
    
    @Schema(description = "排序", required = true, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;
    
    @Schema(description = "权限描述", example = "用户管理相关权限")
    private String description;
} 
