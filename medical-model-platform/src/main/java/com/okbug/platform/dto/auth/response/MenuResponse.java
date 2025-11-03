/**
 * 菜单响应DTO：封装菜单权限信息
 */
package com.okbug.platform.dto.auth.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Data
@Schema(description = "菜单响应")
public class MenuResponse {
    
    @Schema(description = "菜单ID")
    private Long id;
    
    @Schema(description = "菜单名称")
    private String name;
    
    @Schema(description = "菜单路径")
    private String path;
    
    @Schema(description = "组件路径")
    private String component;
    
    @Schema(description = "菜单图标")
    private String icon;
    
    @Schema(description = "子菜单列表")
    private List<MenuResponse> children;
} 
