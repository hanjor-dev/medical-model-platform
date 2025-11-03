/**
 * 模型管理模块API接口
 * 
 * 功能描述：
 * 1. 提供模型管理相关的所有API接口
 * 2. 包括计算任务、模型列表、分类管理、分组管理
 * 3. 统一的错误处理和响应格式
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import request from '@/utils/request'

// 计算任务相关API
export const taskApi = {
  // 获取任务列表
  getTaskList: (params) => {
    return request({
      url: '/api/model-management/task/list',
      method: 'get',
      params
    })
  },

  // 创建任务
  createTask: (data) => {
    return request({
      url: '/api/model-management/task/create',
      method: 'post',
      data
    })
  },

  // 更新任务
  updateTask: (id, data) => {
    return request({
      url: `/api/model-management/task/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除任务
  deleteTask: (id) => {
    return request({
      url: `/api/model-management/task/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取任务详情
  getTaskDetail: (id) => {
    return request({
      url: `/api/model-management/task/detail/${id}`,
      method: 'get'
    })
  }
}

// 模型列表相关API
export const modelApi = {
  // 获取模型列表
  getModelList: (params) => {
    return request({
      url: '/api/model-management/model/list',
      method: 'get',
      params
    })
  },

  // 创建模型
  createModel: (data) => {
    return request({
      url: '/api/model-management/model/create',
      method: 'post',
      data
    })
  },

  // 更新模型
  updateModel: (id, data) => {
    return request({
      url: `/api/model-management/model/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除模型
  deleteModel: (id) => {
    return request({
      url: `/api/model-management/model/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取模型详情
  getModelDetail: (id) => {
    return request({
      url: `/api/model-management/model/detail/${id}`,
      method: 'get'
    })
  }
}

// 模型分类相关API
export const categoryApi = {
  // 获取分类列表
  getCategoryList: (params) => {
    return request({
      url: '/api/model-management/category/list',
      method: 'get',
      params
    })
  },

  // 创建分类
  createCategory: (data) => {
    return request({
      url: '/api/model-management/category/create',
      method: 'post',
      data
    })
  },

  // 更新分类
  updateCategory: (id, data) => {
    return request({
      url: `/api/model-management/category/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除分类
  deleteCategory: (id) => {
    return request({
      url: `/api/model-management/category/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取分类详情
  getCategoryDetail: (id) => {
    return request({
      url: `/api/model-management/category/detail/${id}`,
      method: 'get'
    })
  }
}

// 模型分组相关API
export const groupApi = {
  // 获取分组列表
  getGroupList: (params) => {
    return request({
      url: '/api/model-management/group/list',
      method: 'get',
      params
    })
  },

  // 创建分组
  createGroup: (data) => {
    return request({
      url: '/api/model-management/group/create',
      method: 'post',
      data
    })
  },

  // 更新分组
  updateGroup: (id, data) => {
    return request({
      url: `/api/model-management/group/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除分组
  deleteGroup: (id) => {
    return request({
      url: `/api/model-management/group/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取分组详情
  getGroupDetail: (id) => {
    return request({
      url: `/api/model-management/group/detail/${id}`,
      method: 'get'
    })
  }
}

// 统一导出
export default {
  task: taskApi,
  model: modelApi,
  category: categoryApi,
  group: groupApi
} 