/**
 * 引荐管理API接口
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 21:00:00
 */

import request from '@/utils/request'

export const referralApi = {
  /**
   * 获取当前用户引荐码信息
   * @returns {Promise<Object>} 引荐码信息响应对象
   */
  getReferralCode() {
    return request({
      url: '/api/referral/code',
      method: 'get'
    })
  },

  /**
   * 获取当前用户的引荐用户列表
   * @param {number} page - 页码，从1开始
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 引荐用户列表
   */
  getReferralUsers(page = 1, size = 10) {
    return request({
      url: '/api/referral/users',
      method: 'get',
      params: { page, size }
    })
  }
}
