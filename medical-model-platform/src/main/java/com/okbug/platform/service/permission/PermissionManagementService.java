/**
 * 权限管理服务接口：提供动态权限配置功能
 * 
 * 核心功能：
 * 1. 权限的增删改查
 * 2. 角色权限配置
 * 3. 权限树形结构
 * 4. 权限缓存管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:15:00
 */
package com.okbug.platform.service.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.dto.permission.request.PermissionCreateRequest;
import com.okbug.platform.dto.permission.request.PermissionQueryRequest;
import com.okbug.platform.dto.permission.request.RolePermissionUpdateRequest;
import com.okbug.platform.dto.permission.request.UserPermissionOverrideUpdateRequest;
import com.okbug.platform.dto.permission.response.PermissionTreeResponse;
import com.okbug.platform.dto.permission.response.UserPermissionOverrideResponse;
import com.okbug.platform.entity.auth.Permission;

import java.util.List;

public interface PermissionManagementService {
    
    /**
     * 获取权限树形结构
     * 
     * @return 权限树
     */
    List<PermissionTreeResponse> getPermissionTree();
    
    /**
     * 获取角色的权限树（标记已分配的权限）
     * 
     * @param role 角色
     * @return 权限树（包含是否已分配标记）
     */
    List<PermissionTreeResponse> getRolePermissionTree(String role);
    
    /**
     * 分页查询权限
     * 
     * @param request 查询请求参数
     * @return 权限分页列表
     */
    IPage<Permission> getPermissionPage(PermissionQueryRequest request);
    
    /**
     * 根据ID获取权限详情
     * 
     * @param id 权限ID
     * @return 权限详情
     */
    Permission getPermissionById(Long id);
    
    /**
     * 创建权限
     * 
     * @param request 创建请求
     * @return 权限ID
     */
    Long createPermission(PermissionCreateRequest request);
    
    /**
     * 更新权限
     * 
     * @param id 权限ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updatePermission(Long id, PermissionCreateRequest request);
    
    /**
     * 删除权限
     * 
     * @param id 权限ID
     * @return 是否成功
     */
    boolean deletePermission(Long id);
    
    /**
     * 批量删除权限
     * 
     * @param ids 权限ID列表
     * @return 是否成功
     */
    boolean batchDeletePermissions(List<Long> ids);
    
    /**
     * 启用/禁用权限
     * 
     * @param id 权限ID
     * @param status 状态（0:禁用 1:启用）
     * @return 是否成功
     */
    boolean updatePermissionStatus(Long id, Integer status);
    
    /**
     * 更新角色权限配置
     * 
     * @param request 角色权限配置请求
     * @return 是否成功
     */
    boolean updateRolePermissions(RolePermissionUpdateRequest request);
    
    /**
     * 获取角色的权限列表
     * 
     * @param role 角色
     * @return 权限列表
     */
    List<Permission> getRolePermissions(String role);
    
    /**
     * 检查权限编码是否已存在
     * 
     * @param permissionCode 权限编码
     * @param excludeId 排除的权限ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isPermissionCodeExists(String permissionCode, Long excludeId);
    
    /**
     * 刷新所有用户的权限缓存
     */
    void refreshAllPermissionCache();

    /**
     * 批量更新用户的权限覆盖配置（允许/拒绝）
     * @param request 覆盖请求
     * @return 是否成功
     */
    boolean updateUserPermissionOverrides(UserPermissionOverrideUpdateRequest request);

    /**
     * 获取用户的权限覆盖配置（允许/拒绝）
     * @param userId 用户ID
     * @return 覆盖配置响应
     */
    UserPermissionOverrideResponse getUserPermissionOverrides(Long userId);

    /**
     * 获取用户的生效权限列表（返回完整权限对象）
     * @param userId 用户ID
     * @return 生效权限列表
     */
    List<Permission> getUserEffectivePermissions(Long userId);

    /**
     * 获取用户的生效权限编码列表
     * @param userId 用户ID
     * @return 生效权限编码
     */
    List<String> getUserEffectivePermissionCodes(Long userId);

    /**
     * 获取指定用户的权限树（节点带选中/禁用标记）
     * @param userId 用户ID
     * @return 权限树
     */
    List<PermissionTreeResponse> getUserPermissionTree(Long userId);
} 