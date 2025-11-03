/**
 * 积分系统模块API接口
 * 
 * 功能描述：
 * 1. 提供积分系统相关的所有API接口
 * 2. 包括我的积分、用户积分、类型管理、场景管理
 * 3. 统一的错误处理和响应格式
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import request from '@/utils/request'

// 我的积分相关API
export const myCreditsApi = {
  // 获取我的积分余额
  getMyCreditsBalance: () => {
    return request({
      url: '/api/credits/balance',
      method: 'get'
    })
  },

  // 获取我的积分明细（支持: creditTypeCode, transactionType, scenarioCode, startTimeStr, endTimeStr, keyword）
  getMyCreditsHistory: (params) => {
    return request({
      url: '/api/credits/transactions/my',
      method: 'get',
      params
    })
  },

  // 积分兑换
  exchangeCredits: (data) => {
    return request({
      url: '/api/credits/redeem/use',
      method: 'post',
      params: { codeKey: data?.code }
    })
  },

  // 查询兑换码信息（预览）
  getRedeemInfo: (data) => {
    const codeKey = typeof data === 'string' ? data : (data?.code || data?.codeKey)
    return request({
      url: '/api/credits/redeem/info',
      method: 'get',
      params: { codeKey }
    })
  },

  // 获取我的交易统计
  getMyTransactionStatistics: (params) => {
    return request({
      url: '/api/credits/transactions/my/statistics',
      method: 'get',
      params
    })
  }
}

// 用户积分相关API
export const userCreditsApi = {
  // 获取指定用户的积分余额（管理员）
  getUserBalance: (userId) => {
    return request({
      url: `/api/credits/admin/balance/${userId}`,
      method: 'get'
    })
  },

  // 分配积分给用户
  allocateCredits: (data) => {
    return request({
      url: '/api/credits/admin/allocate',
      method: 'post',
      data
    })
  },

  // 分页查询交易记录（支持指定用户）
  getTransactionsPage: (data) => {
    return request({
      url: '/api/credits/transactions/page',
      method: 'post',
      data
    })
  },

  // 统计交易（根据查询条件）
  getTransactionsStatistics: (data) => {
    return request({
      url: '/api/credits/transactions/statistics',
      method: 'post',
      data
    })
  },

  // 分页查询用户 + 积分账户汇总
  getUserBalancesPage: (data) => {
    return request({
      url: '/api/credits/admin/users/balances/page',
      method: 'post',
      data
    })
  }
}

// 交易类型相关API
export const creditTransactionApi = {
  // 获取交易类型下拉选项
  getTransactionTypeOptions: () => {
    return request({
      url: '/api/credits/transactions/types',
      method: 'get'
    })
  }
}

// 积分类型相关API
export const creditTypeApi = {
  // 获取积分类型列表
  getCreditTypeList: (params) => {
    return request({
      url: '/api/credits/types',
      method: 'get',
      params
    })
  },

  // 获取启用的积分类型（用于下拉）
  getEnabledTypes: () => {
    return request({
      url: '/api/credits/types/enabled',
      method: 'get'
    })
  },

  // 创建积分类型
  createCreditType: (data) => {
    return request({
      url: '/api/credits/types',
      method: 'post',
      data
    })
  },

  // 更新积分类型
  updateCreditType: (id, data) => {
    return request({
      url: `/api/credits/types/${id}`,
      method: 'put',
      data
    })
  },

  // 删除积分类型
  deleteCreditType: (id) => {
    return request({
      url: `/api/credits/types/${id}`,
      method: 'delete'
    })
  },

  // 获取积分类型详情
  getCreditTypeDetail: (id) => {
    return request({
      url: `/api/credits/types/${id}`,
      method: 'get'
    })
  }
}

// 积分场景相关API
export const creditScenarioApi = {
  // 获取积分场景列表
  getCreditScenarioList: (params) => {
    return request({
      url: '/api/credits/scenarios',
      method: 'get',
      params
    })
  },

  // 创建积分场景
  createCreditScenario: (data) => {
    return request({
      url: '/api/credits/scenarios',
      method: 'post',
      data
    })
  },

  // 更新积分场景
  updateCreditScenario: (id, data) => {
    return request({
      url: `/api/credits/scenarios/${id}`,
      method: 'put',
      data
    })
  },

  // 删除积分场景
  deleteCreditScenario: (id) => {
    return request({
      url: `/api/credits/scenarios/${id}`,
      method: 'delete'
    })
  },

  // 获取积分场景详情
  getCreditScenarioDetail: (id) => {
    return request({
      url: `/api/credits/scenarios/${id}`,
      method: 'get'
    })
  }
}

// 兑换码管理（超级管理员）
export const redeemCodeApi = {
  // 生成兑换码
  generate: (data) => {
    const { creditTypeCode, amount, expireTime, remark } = data || {}
    return request({
      url: '/api/credits/redeem/generate',
      method: 'post',
      params: { creditTypeCode, amount, expireTime, remark }
    })
  },
  // 分页列表
  page: (params) => {
    return request({
      url: '/api/credits/redeem/page',
      method: 'get',
      params
    })
  }
}

// 统一导出
export default {
  myCredits: myCreditsApi,
  userCredits: userCreditsApi,
  creditTransaction: creditTransactionApi,
  creditType: creditTypeApi,
  creditScenario: creditScenarioApi,
  redeemCode: redeemCodeApi
} 