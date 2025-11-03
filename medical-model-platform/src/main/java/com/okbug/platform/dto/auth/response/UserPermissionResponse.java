/**
 * 用户权限响应DTO：封装用户权限信息
 * 
 * 功能描述：
 * 1. 封装用户角色和权限列表
 * 2. 用于权限验证和前端权限控制
 * 3. 支持权限缓存和快速查询
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:35:00
 */
package com.okbug.platform.dto.auth.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Schema(description = "用户权限响应")
public class UserPermissionResponse {
    
    /**
     * 用户角色
     */
    @Schema(description = "用户角色", example = "USER")
    private String role;
    
    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private List<PermissionInfo> permissions;
    
    /**
     * 权限信息
     */
    @Data
    @Schema(description = "权限信息")
    public static class PermissionInfo {
        
        /**
         * 权限编码
         */
        @Schema(description = "权限编码", example = "user:profile:view")
        private String code;
        
        /**
         * 权限名称
         */
        @Schema(description = "权限名称", example = "查看用户信息")
        private String name;
        
        /**
         * 权限类型
         */
        @Schema(description = "权限类型", example = "BUTTON")
        private String type;
        
        public static PermissionInfo create(String code, String name, String type) {
            PermissionInfo info = new PermissionInfo();
            info.setCode(code);
            info.setName(name);
            info.setType(type);
            return info;
        }
    }
    
    /**
     * 创建用户权限响应
     */
    public static UserPermissionResponse create(String role, List<PermissionInfo> permissions) {
        UserPermissionResponse response = new UserPermissionResponse();
        response.setRole(role);
        response.setPermissions(permissions);
        return response;
    }
} 
