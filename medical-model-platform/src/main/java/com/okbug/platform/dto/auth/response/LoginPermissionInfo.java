/**
 * 登录权限信息DTO：专门为登录接口设计的权限信息类
 * 
 * 功能描述：
 * 1. 封装用户登录后返回的完整权限信息
 * 2. 包含前端动态生成菜单所需的所有字段
 * 3. 支持权限树形结构构建
 * 4. 提供权限验证和菜单生成的基础数据
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:30:00
 */
package com.okbug.platform.dto.auth.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "登录权限信息")
public class LoginPermissionInfo {
    
    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1234567890")
    private Long id;
    
    /**
     * 权限编码（唯一标识）
     */
    @Schema(description = "权限编码", example = "user:profile:view")
    private String code;
    
    /**
     * 权限名称
     */
    @Schema(description = "权限名称", example = "查看用户信息")
    private String name;
    
    /**
     * 权限类型（MENU:菜单 BUTTON:按钮 API:接口）
     */
    @Schema(description = "权限类型", example = "MENU")
    private String type;
    
    /**
     * 父权限ID（用于构建权限树）
     */
    @Schema(description = "父权限ID", example = "1234567890")
    private Long parentId;
    
    /**
     * 菜单路径（前端路由路径）
     */
    @Schema(description = "菜单路径", example = "/user/profile")
    private String path;
    
    /**
     * 组件路径（前端组件路径）
     */
    @Schema(description = "组件路径", example = "views/user/Profile.vue")
    private String component;
    
    /**
     * 图标（菜单图标）
     */
    @Schema(description = "图标", example = "user")
    private String icon;
    
    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sort;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    // ================ 便利方法 ================
    
    /**
     * 创建权限信息对象
     */
    public static LoginPermissionInfo create(Long id, String code, String name, String type, 
                                           Long parentId, String path, String component, 
                                           String icon, Integer sort, Integer status) {
        LoginPermissionInfo info = new LoginPermissionInfo();
        info.setId(id);
        info.setCode(code);
        info.setName(name);
        info.setType(type);
        info.setParentId(parentId);
        info.setPath(path);
        info.setComponent(component);
        info.setIcon(icon);
        info.setSort(sort);
        info.setStatus(status);
        return info;
    }
    
    /**
     * 判断是否为菜单权限
     */
    public boolean isMenu() {
        return "MENU".equals(this.type);
    }
    
    /**
     * 判断是否为按钮权限
     */
    public boolean isButton() {
        return "BUTTON".equals(this.type);
    }
    
    /**
     * 判断是否为接口权限
     */
    public boolean isApi() {
        return "API".equals(this.type);
    }
    
    /**
     * 判断是否为顶级权限（没有父权限）
     */
    public boolean isRoot() {
        return this.parentId == null || this.parentId == 0;
    }
    
    /**
     * 判断权限是否已启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
} 
