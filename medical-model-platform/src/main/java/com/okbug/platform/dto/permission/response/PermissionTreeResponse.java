/**
 * 权限树形结构响应DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:10:00
 */
package com.okbug.platform.dto.permission.response;

import com.okbug.platform.entity.auth.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "权限树形结构")
public class PermissionTreeResponse {
    
    @Schema(description = "权限ID", example = "1")
    private Long id;
    
    @Schema(description = "权限编码", example = "system:user")
    private String permissionCode;
    
    @Schema(description = "权限名称", example = "用户管理")
    private String permissionName;
    
    @Schema(description = "权限类型", example = "MENU")
    private String permissionType;
    
    @Schema(description = "父权限ID", example = "0")
    private Long parentId;
    
    @Schema(description = "菜单路径", example = "/system/user")
    private String menuPath;
    
    @Schema(description = "组件路径", example = "system/user/index")
    private String componentPath;
    
    @Schema(description = "菜单图标", example = "user")
    private String menuIcon;
    
    @Schema(description = "排序", example = "1")
    private Integer sort;
    
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    @Schema(description = "权限描述", example = "用户管理相关权限")
    private String description;
    
    @Schema(description = "是否选中", example = "false")
    private Boolean selected;

    @Schema(description = "是否禁用", example = "false")
    private Boolean disabled;

    @Schema(description = "子权限列表")
    private List<PermissionTreeResponse> children = new ArrayList<>();
    
    /**
     * 从Permission实体创建PermissionTreeResponse
     */
    public static PermissionTreeResponse fromPermission(Permission permission) {
        PermissionTreeResponse response = new PermissionTreeResponse();
        response.setId(permission.getId());
        response.setPermissionCode(permission.getPermissionCode());
        response.setPermissionName(permission.getPermissionName());
        response.setPermissionType(permission.getPermissionType());
        response.setParentId(permission.getParentId());
        response.setMenuPath(permission.getPath());
        response.setComponentPath(permission.getComponent());
        response.setMenuIcon(permission.getIcon());
        response.setSort(permission.getSort());
        response.setStatus(permission.getStatus());
        response.setDescription(null); // Permission实体暂无描述字段
        response.setSelected(Boolean.FALSE);
        response.setDisabled(Boolean.FALSE);
        response.setChildren(new ArrayList<>());
        return response;
    }
} 
