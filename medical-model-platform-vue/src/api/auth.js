import request from '@/utils/request'

export const authApi = {
  /**
   * 用户登录
   * @param {Object} credentials - 登录凭据
   * @param {string} credentials.username - 用户名或邮箱
   * @param {string} credentials.password - 密码
   * @returns {Promise<Object>} 登录结果
   */
  login(credentials) {
    return request({
      url: '/api/auth/login',
      method: 'post',
      data: credentials
    })
  },

  /**
   * 用户注册
   * @param {Object} userData - 用户注册数据
   * @param {string} userData.username - 用户名
   * @param {string} userData.password - 密码
   * @param {string} userData.email - 邮箱（可选）
   * @param {string} userData.nickname - 昵称（可选）
   * @param {string} userData.referralCode - 推荐码（可选）
   * @param {string} userData.teamCode - 团队码（可选，与推荐码互斥）
   * @returns {Promise<Object>} 注册结果
   */
  register(userData) {
    return request({
      url: '/api/auth/register',
      method: 'post',
      data: userData
    })
  },

  /**
   * 用户登出
   * @returns {Promise<Object>} 登出结果
   */
  logout() {
    return request({
      url: '/api/auth/logout',
      method: 'post'
    })
  },

  /**
   * 获取当前用户信息
   * @returns {Promise<Object>} 用户信息
   */
  getCurrentUser() {
    return request({
      url: '/api/auth/session',
      method: 'get'
    })
  },

  /**
   * 刷新访问令牌
   * @returns {Promise<Object>} 刷新结果
   */
  refreshToken() {
    return request({
      url: '/api/auth/refresh',
      method: 'post'
    })
  },

  /**
   * 忘记密码
   * @param {Object} data - 忘记密码数据
   * @param {string} data.email - 邮箱地址
   * @returns {Promise<Object>} 忘记密码结果
   */
  forgotPassword(data) {
    return request({
      url: '/api/auth/forgot-password',
      method: 'post',
      data
    })
  },

  /**
   * 重置密码
   * @param {Object} data - 重置密码数据
   * @param {string} data.token - 重置令牌
   * @param {string} data.newPassword - 新密码
   * @param {string} data.confirmPassword - 确认新密码
   * @returns {Promise<Object>} 重置密码结果
   */
  resetPassword(data) {
    return request({
      url: '/api/auth/reset-password',
      method: 'post',
      data
    })
  }
} 