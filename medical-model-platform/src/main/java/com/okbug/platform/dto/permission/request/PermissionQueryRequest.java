/**
 * 权限查询请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 13:10:00
 */
package com.okbug.platform.dto.permission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@Schema(description = "权限查询请求")
public class PermissionQueryRequest {
    
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;
    
    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer size = 10;
    
    @Schema(description = "权限类型", example = "MENU")
    @Pattern(regexp = "^(MENU|BUTTON|API)?$", message = "权限类型只能是MENU、BUTTON、API或空")
    private String permissionType;
    
    @Schema(description = "关键词", example = "user")
    private String keyword;
} 
