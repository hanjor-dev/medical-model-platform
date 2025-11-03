/**
 * 权限管理服务接口：定义权限相关的业务操作
 * 
 * 功能描述：
 * 1. 用户权限查询和验证
 * 2. 菜单权限构建
 * 3. 数据权限控制
 * 4. 权限缓存管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:30:00
 */
package com.okbug.platform.service.permission;

import com.okbug.platform.dto.auth.response.UserPermissionResponse;
import com.okbug.platform.dto.auth.response.MenuResponse;
import com.okbug.platform.dto.auth.response.LoginPermissionInfo;
import java.util.List;

public interface PermissionService {
    
    /**
     * 获取当前用户的权限信息
     * 
     * @return 用户权限响应
     */
    UserPermissionResponse getCurrentUserPermissions();
    
    /**
     * 获取当前用户的菜单权限
     * 
     * @return 菜单树列表
     */
    List<MenuResponse> getCurrentUserMenus();
    
    /**
     * 检查当前用户是否拥有指定权限
     * 
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(String permissionCode);
    
    /**
     * 检查当前用户是否拥有指定角色
     * 
     * @param role 角色
     * @return 是否拥有角色
     */
    boolean hasRole(String role);
    
    /**
     * 检查当前用户是否有访问指定用户数据的权限
     * 
     * @param targetUserId 目标用户ID
     * @return 是否有数据权限
     */
    boolean hasDataPermission(Long targetUserId);
    
    /**
     * 根据角色获取权限列表（用于后台查看/配置），不影响生效计算
     * @param role 角色
     * @return 权限编码列表
     */
    List<String> getPermissionsByRole(String role);
    
    /**
     * 刷新权限缓存
     * 
     * @param userId 用户ID，null表示刷新所有用户
     */
    void refreshPermissionCache(Long userId);
    
    /**
     * 获取登录用户的完整权限信息
     * 专门为登录接口设计，返回包含前端菜单生成所需的所有字段
     * 
     * @param userId 用户ID
     * @return 完整的权限信息列表
     */
    List<LoginPermissionInfo> getLoginUserPermissions(Long userId);

    /**
     * 获取用户的生效权限编码列表
     * 规则：仅读取 user_permissions 中 ALLOW 的权限；超级管理员若无显式分配则授予全部启用权限
     *
     * @param userId 用户ID
     * @return 生效权限编码列表
     */
    List<String> getEffectivePermissionCodes(Long userId);
} 