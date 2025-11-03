import axios from 'axios'

/**
 * 系统配置管理API
 */

/**
 * 获取配置列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @param {string} params.configCategory - 配置分类（名称，不推荐）
 * @param {string} params.configCategoryCode - 配置分类编码（字典码，推荐）
 * @param {string} params.configType - 配置类型
 * @param {string} params.configKey - 配置键（模糊搜索）
 * @param {string} params.description - 描述关键词（模糊搜索）
 * @param {number} params.status - 状态 (0=禁用, 1=启用)
 * @param {string} params.sortField - 排序字段
 * @param {string} params.sortDirection - 排序方向 (asc/desc)
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigList = async (params = {}) => {
  try {
    const response = await axios.get('/api/system/configs', { params })
    return response.data
  } catch (error) {
    console.error('获取配置列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取配置详情
 * @param {number|string} id - 配置ID
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigById = async (id) => {
  try {
    const response = await axios.get(`/api/system/configs/${encodeURIComponent(String(id))}`)
    return response.data
  } catch (error) {
    console.error('获取配置详情失败:', error)
    throw error
  }
}

/**
 * 根据配置键获取配置详情
 * @param {string} configKey - 配置键
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigByKey = async (configKey) => {
  try {
    const response = await axios.get(`/api/system/configs/key/${encodeURIComponent(String(configKey))}`)
    return response.data
  } catch (error) {
    console.error('根据配置键获取配置失败:', error)
    throw error
  }
}

/**
 * 创建新配置
 * @param {Object} data - 配置数据
 * @param {string} data.configKey - 配置键
 * @param {string} data.configValue - 配置值
 * @param {string} data.configType - 配置类型 (STRING/NUMBER/BOOLEAN/JSON)
 * @param {string} data.configCategory - 配置分类 (SYSTEM/USER/FILE/TASK/SECURITY/CACHE)
 * @param {string} data.description - 配置描述
 * @param {number} data.status - 状态 (0=禁用, 1=启用)
 * @param {number} data.sortOrder - 排序
 * @returns {Promise<Object>} API响应数据
 */
export const createConfig = async (data) => {
  try {
    const response = await axios.post('/api/system/configs', data)
    return response.data
  } catch (error) {
    console.error('创建配置失败:', error)
    throw error
  }
}

/**
 * 更新配置
 * @param {number|string} id - 配置ID
 * @param {Object} data - 配置数据
 * @param {string} data.configValue - 配置值
 * @param {string} data.description - 配置描述
 * @param {number} data.status - 状态
 * @param {number} data.sortOrder - 排序
 * @returns {Promise<Object>} API响应数据
 */
export const updateConfig = async (id, data) => {
  try {
    const response = await axios.put(`/api/system/configs/${encodeURIComponent(String(id))}`, data)
    return response.data
  } catch (error) {
    console.error('更新配置失败:', error)
    throw error
  }
}

/**
 * 删除配置
 * @param {number|string} id - 配置ID
 * @returns {Promise<Object>} API响应数据
 */
export const deleteConfig = async (id) => {
  try {
    const response = await axios.delete(`/api/system/configs/${encodeURIComponent(String(id))}`)
    return response.data
  } catch (error) {
    console.error('删除配置失败:', error)
    throw error
  }
}

/**
 * 批量删除配置
 * @param {Array<number|string>} ids - 配置ID数组
 * @returns {Promise<Object>} API响应数据
 */
export const batchDeleteConfigs = async (ids) => {
  try {
    const response = await axios.delete('/api/system/configs/batch', {
      data: ids
    })
    return response.data
  } catch (error) {
    console.error('批量删除配置失败:', error)
    throw error
  }
}

/**
 * 批量更新配置状态
 * @param {Array<number|string>} ids - 配置ID数组
 * @param {number} status - 目标状态 (0=禁用, 1=启用)
 * @returns {Promise<Object>} API响应数据
 */
export const batchUpdateStatus = async (ids, status) => {
  try {
    const response = await axios.put('/api/system/configs/batch/status', ids, {
      params: { status }
    })
    return response.data
  } catch (error) {
    console.error('批量更新配置状态失败:', error)
    throw error
  }
}

/**
 * 批量更新配置排序
 * @param {Object} idSortMap - 配置ID和排序值的映射关系
 * @returns {Promise<Object>} API响应数据
 */
export const batchUpdateSortOrder = async (idSortMap) => {
  try {
    const response = await axios.put('/api/system/configs/batch/sort', idSortMap)
    return response.data
  } catch (error) {
    console.error('批量更新配置排序失败:', error)
    throw error
  }
}

/**
 * 获取配置值（字符串）
 * @param {string} configKey - 配置键
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValue = async (configKey) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}`)
    return response.data
  } catch (error) {
    console.error('获取配置值失败:', error)
    throw error
  }
}

/**
 * 获取配置值（带默认值）
 * @param {string} configKey - 配置键
 * @param {string} defaultValue - 默认值
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueWithDefault = async (configKey, defaultValue) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/default`, {
      params: { defaultValue }
    })
    return response.data
  } catch (error) {
    console.error('获取配置值（带默认值）失败:', error)
    throw error
  }
}

/**
 * 获取配置值（整数）
 * @param {string} configKey - 配置键
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueAsInt = async (configKey) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/int`)
    return response.data
  } catch (error) {
    console.error('获取配置值（整数）失败:', error)
    throw error
  }
}

/**
 * 获取配置值（整数，带默认值）
 * @param {string} configKey - 配置键
 * @param {number} defaultValue - 默认值
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueAsIntWithDefault = async (configKey, defaultValue) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/int/default`, {
      params: { defaultValue }
    })
    return response.data
  } catch (error) {
    console.error('获取配置值（整数，带默认值）失败:', error)
    throw error
  }
}

/**
 * 获取配置值（布尔值）
 * @param {string} configKey - 配置键
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueAsBoolean = async (configKey) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/boolean`)
    return response.data
  } catch (error) {
    console.error('获取配置值（布尔值）失败:', error)
    throw error
  }
}

/**
 * 获取配置值（布尔值，带默认值）
 * @param {string} configKey - 配置键
 * @param {boolean} defaultValue - 默认值
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueAsBooleanWithDefault = async (configKey, defaultValue) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/boolean/default`, {
      params: { defaultValue }
    })
    return response.data
  } catch (error) {
    console.error('获取配置值（布尔值，带默认值）失败:', error)
    throw error
  }
}

/**
 * 获取配置值（JSON对象）
 * @param {string} configKey - 配置键
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigValueAsJson = async (configKey) => {
  try {
    const response = await axios.get(`/api/system/configs/value/${encodeURIComponent(String(configKey))}/json`)
    return response.data
  } catch (error) {
    console.error('获取配置值（JSON对象）失败:', error)
    throw error
  }
}

/**
 * 获取所有配置分类
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigCategories = async () => {
  try {
    const response = await axios.get('/api/system/configs/categories')
    return response.data
  } catch (error) {
    console.error('获取配置分类失败:', error)
    throw error
  }
}

/**
 * 根据配置分类获取配置列表
 * @param {string} category - 配置分类
 * @returns {Promise<Object>} API响应数据
 */
export const getConfigsByCategory = async (category) => {
  try {
    const response = await axios.get(`/api/system/configs/categories/${encodeURIComponent(String(category))}`)
    return response.data
  } catch (error) {
    console.error('根据配置分类获取配置列表失败:', error)
    throw error
  }
}

/**
 * 刷新配置缓存
 * @returns {Promise<Object>} API响应数据
 */
export const refreshConfigCache = async () => {
  try {
    const response = await axios.post('/api/system/configs/cache/refresh')
    return response.data
  } catch (error) {
    console.error('刷新配置缓存失败:', error)
    throw error
  }
}

/**
 * 清除配置缓存
 * @param {string} configKey - 配置键（可选）
 * @returns {Promise<Object>} API响应数据
 */
export const clearConfigCache = async (configKey = null) => {
  try {
    const params = configKey ? { configKey } : {}
    const response = await axios.delete('/api/system/configs/cache', { params })
    return response.data
  } catch (error) {
    console.error('清除配置缓存失败:', error)
    throw error
  }
}

// 默认导出所有API方法
export default {
  getConfigList,
  getConfigById,
  getConfigByKey,
  createConfig,
  updateConfig,
  deleteConfig,
  batchDeleteConfigs,
  batchUpdateStatus,
  batchUpdateSortOrder,
  getConfigValue,
  getConfigValueWithDefault,
  getConfigValueAsInt,
  getConfigValueAsIntWithDefault,
  getConfigValueAsBoolean,
  getConfigValueAsBooleanWithDefault,
  getConfigValueAsJson,
  getConfigCategories,
  getConfigsByCategory,
  refreshConfigCache,
  clearConfigCache
}
