/**
 * AI引擎模块API接口
 * 
 * 功能描述：
 * 1. 提供AI引擎相关的所有API接口
 * 2. 包括算法管理、多端配置
 * 3. 统一的错误处理和响应格式
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import request from '@/utils/request'

// 算法管理相关API
export const algorithmApi = {
  // 获取算法列表
  getAlgorithmList: (params) => {
    return request({
      url: '/api/ai-engine/algorithm/list',
      method: 'get',
      params
    })
  },

  // 创建算法
  createAlgorithm: (data) => {
    return request({
      url: '/api/ai-engine/algorithm/create',
      method: 'post',
      data
    })
  },

  // 更新算法
  updateAlgorithm: (id, data) => {
    return request({
      url: `/api/ai-engine/algorithm/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除算法
  deleteAlgorithm: (id) => {
    return request({
      url: `/api/ai-engine/algorithm/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取算法详情
  getAlgorithmDetail: (id) => {
    return request({
      url: `/api/ai-engine/algorithm/detail/${id}`,
      method: 'get'
    })
  },

  // 算法部署
  deployAlgorithm: (id) => {
    return request({
      url: `/api/ai-engine/algorithm/deploy/${id}`,
      method: 'post'
    })
  },

  // 算法停止
  stopAlgorithm: (id) => {
    return request({
      url: `/api/ai-engine/algorithm/stop/${id}`,
      method: 'post'
    })
  }
}

// 多端配置相关API
export const multiConfigApi = {
  // 获取多端配置列表
  getMultiConfigList: (params) => {
    return request({
      url: '/api/ai-engine/multi-config/list',
      method: 'get',
      params
    })
  },

  // 创建多端配置
  createMultiConfig: (data) => {
    return request({
      url: '/api/ai-engine/multi-config/create',
      method: 'post',
      data
    })
  },

  // 更新多端配置
  updateMultiConfig: (id, data) => {
    return request({
      url: `/api/ai-engine/multi-config/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除多端配置
  deleteMultiConfig: (id) => {
    return request({
      url: `/api/ai-engine/multi-config/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取多端配置详情
  getMultiConfigDetail: (id) => {
    return request({
      url: `/api/ai-engine/multi-config/detail/${id}`,
      method: 'get'
    })
  },

  // 配置同步
  syncMultiConfig: (id) => {
    return request({
      url: `/api/ai-engine/multi-config/sync/${id}`,
      method: 'post'
    })
  },

  // 配置验证
  validateMultiConfig: (data) => {
    return request({
      url: '/api/ai-engine/multi-config/validate',
      method: 'post',
      data
    })
  }
}

// 统一导出
export default {
  algorithm: algorithmApi,
  multiConfig: multiConfigApi
} 