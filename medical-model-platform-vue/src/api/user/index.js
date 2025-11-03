/**
 * 用户管理API接口
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 20:10:00
 */

import request from '@/utils/request'

export const userApi = {
  /**
   * 获取用户详细信息
   * @returns {Promise<Object>} 用户详细信息
   */
  getUserProfile() {
    return request({
      url: '/api/user/profile',
      method: 'get'
    })
  },

  /**
   * 获取单个用户信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户信息
   */
  getUserById(userId) {
    return request({
      url: `/api/user/${userId}`,
      method: 'get'
    })
  },

  /**
   * 更新用户基本信息
   * @param {Object} data - 用户信息数据
   * @returns {Promise<Object>} 更新结果
   */
  updateUserProfile(data) {
    return request({
      url: '/api/user/profile',
      method: 'put',
      data
    })
  },

  /**
   * 上传用户头像
   * @param {FormData} formData - 包含头像文件的FormData
   * @returns {Promise<Object>} 上传结果
   */
  uploadUserAvatar(formData) {
    return request({
      url: '/api/user/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 修改用户密码
   * @param {Object} data - 密码修改数据
   * @returns {Promise<Object>} 修改结果
   */
  changePassword(data) {
    return request({
      url: '/api/user/password',
      method: 'put',
      data
    })
  },

  /**
   * 获取用户列表（管理员功能）
   * @param {Object} params - 查询参数
   * @returns {Promise<Object>} 用户列表
   */
  getUserList(params) {
    return request({
      url: '/api/user/children',
      method: 'get',
      params
    })
  },

  /**
   * 创建子账号（管理员功能）
   * @param {Object} data - 用户注册数据
   * @returns {Promise<Object>} 创建结果
   */
  createSubUser(data) {
    return request({
      url: '/api/user/children',
      method: 'post',
      data
    })
  },

  /**
   * 更新子账号（管理员功能）
   * @param {number} userId - 用户ID
   * @param {Object} data - 更新数据
   */
  updateSubUser(userId, data) {
    return request({
      url: `/api/user/children/${userId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除子账号（管理员功能）
   * @param {number} userId - 用户ID
   */
  deleteSubUser(userId) {
    return request({
      url: `/api/user/children/${userId}`,
      method: 'delete'
    })
  },

  /**
   * 切换子账号状态（管理员功能）
   * @param {number} userId - 用户ID
   * @param {number} status - 0禁用/1启用
   */
  toggleSubUserStatus(userId, status) {
    return request({
      url: `/api/user/children/${userId}/status`,
      method: 'put',
      params: { status }
    })
  },

  /**
   * 重置子账号密码（管理员功能）
   * @param {number} userId - 用户ID
   * @param {string} newPassword - 新密码
   */
  resetSubUserPassword(userId) {
    return request({
      url: `/api/user/children/${userId}/password`,
      method: 'put'
    })
  },

  /**
   * 创建用户（管理员功能）
   * @param {Object} data - 用户创建数据
   * @returns {Promise<Object>} 创建结果
   */
  createUser(data) {
    return request({
      url: '/api/user',
      method: 'post',
      data
    })
  },

  /**
   * 更新用户信息（管理员功能）
   * @param {number} userId - 用户ID
   * @param {Object} data - 用户更新数据
   * @returns {Promise<Object>} 更新结果
   */
  updateUser(userId, data) {
    return request({
      url: `/api/user/${userId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除用户（管理员功能）
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 删除结果
   */
  deleteUser(userId) {
    return request({
      url: `/api/user/${userId}`,
      method: 'delete'
    })
  },

  /**
   * 获取用户角色列表
   * @returns {Promise<Object>} 角色列表
   */
  getUserRoles() {
    return request({
      url: '/api/user/roles',
      method: 'get'
    })
  },

  /**
   * 分配用户角色（管理员功能）
   * @param {number} userId - 用户ID
   * @param {Array} roleIds - 角色ID数组
   * @returns {Promise<Object>} 分配结果
   */
  assignUserRoles(userId, roleIds) {
    return request({
      url: `/api/user/${userId}/roles`,
      method: 'put',
      data: { roleIds }
    })
  },

  // ==================== 用户日志相关接口 ====================

  /**
   * 分页查询用户日志
   * @param {Object} params - 查询参数
   * @returns {Promise<Object>} 日志列表
   */
  getUserLogs(params) {
    return request({
      url: '/api/user-logs',
      method: 'get',
      params
    })
  },

  /**
   * 获取日志详情
   * @param {number} logId - 日志ID
   * @returns {Promise<Object>} 日志详情
   */
  getLogDetail(logId) {
    return request({
      url: `/api/user-logs/${logId}`,
      method: 'get'
    })
  },

  /**
   * 获取操作模块选项
   * @returns {Promise<Object>} 模块选项列表
   */
  getLogModules() {
    return request({
      url: '/api/user-logs/modules',
      method: 'get'
    })
  },

  

  /**
   * 导出日志数据
   * @param {Object} params - 导出参数
   * @returns {Promise<Object>} 导出结果
   */
  exportLogs(params) {
    return request({
      url: '/api/user-logs/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  /**
   * 清理过期日志
   * @param {Object} params - 清理参数
   * @returns {Promise<Object>} 清理结果
   */
  cleanExpiredLogs(params) {
    return request({
      url: '/api/user-logs/expired',
      method: 'delete',
      data: params
    })
  },

  /**
   * 获取查询帮助信息
   * @returns {Promise<Object>} 帮助信息
   */
  getLogHelp() {
    return request({
      url: '/api/user-logs/help',
      method: 'get'
    })
  }
} 