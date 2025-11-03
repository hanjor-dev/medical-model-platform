/**
 * 权限相关API
 */
import request from '@/utils/request'

export const permissionApi = {
  /**
   * 获取指定用户的生效权限（完整对象，含名称）
   * @param {number} userId 用户ID
   */
  getUserEffectivePermissions(userId) {
    return request({
      url: `/api/permissions/user/${userId}/effective`,
      method: 'get'
    })
  },

  /**
   * 获取指定用户的生效权限编码（如需）
   * @param {number} userId 用户ID
   */
  getUserEffectivePermissionCodes(userId) {
    return request({
      url: `/api/permissions/user/${userId}/effective/codes`,
      method: 'get'
    })
  },

  // 获取权限树
  getPermissionTree() {
    return request({
      url: '/api/permissions/tree',
      method: 'get'
    })
  },

  // 获取角色权限树
  getRolePermissionTree(role) {
    return request({
      url: `/api/permissions/role/${role}/tree`,
      method: 'get'
    })
  },

  // 配置角色权限
  updateRolePermissions(data) {
    return request({
      url: '/api/permissions/role',
      method: 'put',
      data
    })
  },

  // 获取角色权限列表（返回完整权限对象）
  getRolePermissions(role) {
    return request({
      url: `/api/permissions/role/${role}`,
      method: 'get'
    })
  },

  // 分页查询权限
  getPermissionPage(params) {
    return request({
      url: '/api/permissions',
      method: 'get',
      params
    })
  },

  // 创建权限
  createPermission(data) {
    return request({
      url: '/api/permissions',
      method: 'post',
      data
    })
  },

  // 更新权限
  updatePermission(id, data) {
    return request({
      url: `/api/permissions/${id}`,
      method: 'put',
      data
    })
  },

  // 删除权限
  deletePermission(id) {
    return request({
      url: `/api/permissions/${id}`,
      method: 'delete'
    })
  },

  // 批量删除权限
  batchDeletePermissions(ids) {
    return request({
      url: '/api/permissions/batch',
      method: 'delete',
      data: ids
    })
  },

  // 更新权限状态
  updatePermissionStatus(id, status) {
    return request({
      url: `/api/permissions/${id}/status`,
      method: 'put',
      params: { status }
    })
  },

  // 刷新权限缓存
  refreshPermissionCache() {
    return request({
      url: '/api/permissions/cache/refresh',
      method: 'post'
    })
  },

  // 获取全局权限版本
  getPermissionVersion() {
    return request({
      url: '/api/permissions/version',
      method: 'get'
    })
  },

  // 获取用户权限覆盖（allow/deny）
  getUserOverrides(userId) {
    return request({
      url: `/api/permissions/user/${userId}/overrides`,
      method: 'get'
    })
  },

  // 更新用户权限覆盖（仅allow生效）
  updateUserOverrides(data) {
    return request({
      url: '/api/permissions/user/overrides',
      method: 'put',
      data
    })
  }
}


