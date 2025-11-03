import axios from 'axios'

/**
 * 字典数据管理API
 */

/**
 * 获取字典列表
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.dictCode - 字典编码
 * @param {string} params.module - 所属模块
 * @param {number} params.status - 状态 (0=禁用, 1=启用)
 * @param {number} params.level - 层级
 * @returns {Promise<Object>} API响应数据
 */
export const getDictList = async (params = {}) => {
  try {
    const response = await axios.get('/api/system/dict', { params })
    return response.data
  } catch (error) {
    console.error('获取字典列表失败:', error)
    throw error
  }
}

/**
 * 新增字典
 * @param {Object} data - 字典数据
 * @param {string} data.dictName - 字典名称
 * @param {string} data.dictLabel - 字典标签
 * @param {string} data.module - 所属模块
 * @param {number} data.parentId - 父级字典ID（可选，不填则为顶级）
 * @param {number} data.sortOrder - 排序
 * @param {number} data.status - 状态
 * @param {string} data.description - 描述
 * @returns {Promise<Object>} API响应数据
 */
export const createDict = async (data) => {
  try {
    const response = await axios.post('/api/system/dict', data)
    return response.data
  } catch (error) {
    console.error('新增字典失败:', error)
    throw error
  }
}

/**
 * 更新字典
 * @param {number|string} id - 字典ID
 * @param {Object} data - 字典数据
 * @returns {Promise<Object>} API响应数据
 */
export const updateDict = async (id, data) => {
  try {
    const response = await axios.put(`/api/system/dict/${id}`, data)
    return response.data
  } catch (error) {
    console.error('更新字典失败:', error)
    throw error
  }
}

/**
 * 删除字典
 * @param {number|string} id - 字典ID
 * @returns {Promise<Object>} API响应数据
 */
export const deleteDict = async (id) => {
  try {
    const response = await axios.delete(`/api/system/dict/${id}`)
    return response.data
  } catch (error) {
    console.error('删除字典失败:', error)
    throw error
  }
}

/**
 * 批量删除字典
 * @param {Array<number|string>} ids - 字典ID数组
 * @returns {Promise<Object>} API响应数据
 */
export const batchDeleteDict = async (ids) => {
  try {
    const response = await axios.request({
      method: 'DELETE',
      url: '/api/system/dict/batch',
      data: { ids }
    })
    return response.data
  } catch (error) {
    console.error('批量删除字典失败:', error)
    throw error
  }
}

/**
 * 更新字典状态
 * @param {number|string} id - 字典ID
 * @param {number} status - 状态 (0=禁用, 1=启用)
 * @returns {Promise<Object>} API响应数据
 */
export const updateDictStatus = async (id, status) => {
  try {
    const response = await axios.put(`/api/system/dict/${id}/status`, { status })
    return response.data
  } catch (error) {
    console.error('更新字典状态失败:', error)
    throw error
  }
}

/**
 * 导出选中字典
 * @param {Array<number|string>} ids - 字典ID数组
 * @returns {Promise<Blob>} 文件流
 */
export const exportSelectedDict = async (ids) => {
  try {
    const response = await axios.post('/api/system/dict/export', 
      { ids },
      { responseType: 'blob' }
    )
    return response.data
  } catch (error) {
    console.error('导出选中字典失败:', error)
    throw error
  }
}

/**
 * 导出全部字典
 * @param {Object} params - 筛选参数
 * @returns {Promise<Blob>} 文件流
 */
export const exportAllDict = async (params = {}) => {
  try {
    const response = await axios.get('/api/system/dict/export/all', { 
      params,
      responseType: 'blob'
    })
    return response.data
  } catch (error) {
    console.error('导出全部字典失败:', error)
    throw error
  }
}

/**
 * 批量启用字典
 * @param {Array<number|string>} ids - 字典ID数组
 * @returns {Promise<Object>} API响应数据
 */
export const batchEnableDict = async (ids) => {
  try {
    const response = await axios.put('/api/system/dict/batch/enable', { ids })
    return response.data
  } catch (error) {
    console.error('批量启用字典失败:', error)
    throw error
  }
}

/**
 * 批量禁用字典
 * @param {Array<number|string>} ids - 字典ID数组
 * @returns {Promise<Object>} API响应数据
 */
export const batchDisableDict = async (ids) => {
  try {
    const response = await axios.put('/api/system/dict/batch/disable', { ids })
    return response.data
  } catch (error) {
    console.error('批量禁用字典失败:', error)
    throw error
  }
}

/**
 * 获取字典详情
 * @param {number|string} id - 字典ID
 * @returns {Promise<Object>} API响应数据
 */
export const getDictDetail = async (id) => {
  try {
    const response = await axios.get(`/api/system/dict/${id}`)
    return response.data
  } catch (error) {
    console.error('获取字典详情失败:', error)
    throw error
  }
}

/**
 * 导入字典数据
 * @param {File} file - Excel文件
 * @param {boolean} overwrite - 是否覆盖已存在的数据
 * @returns {Promise<Object>} API响应数据
 */
export const importDict = async (file, overwrite = false) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('overwrite', overwrite)
    
    const response = await axios.post('/api/system/dict/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  } catch (error) {
    console.error('导入字典数据失败:', error)
    throw error
  }
}

/**
 * 刷新字典缓存
 * @returns {Promise<Object>} API响应数据
 */
export const refreshDictCache = async () => {
  try {
    const response = await axios.post('/api/system/dict/cache/refresh')
    return response.data
  } catch (error) {
    console.error('刷新字典缓存失败:', error)
    throw error
  }
}

/**
 * 获取模块列表（旧版本，返回字符串数组）
 * @returns {Promise<Object>} API响应数据
 * @deprecated 建议使用 getDictModuleOptions 方法获取对象格式的模块列表
 */
export const getModules = async () => {
  try {
    const response = await axios.get('/api/system/dict/modules')
    return response.data
  } catch (error) {
    console.error('获取模块列表失败:', error)
    throw error
  }
}

/**
 * 获取字典模块选项列表（用于前端下拉选择器）
 * @returns {Promise<Object>} API响应数据，格式：[{value: "SYSTEM", label: "系统模块"}, {value: "USER", label: "用户模块"}]
 */
export const getDictModules = async () => {
  try {
    const response = await axios.get('/api/system/dict/dict-modules')
    return response.data
  } catch (error) {
    console.error('获取字典模块选项列表失败:', error)
    throw error
  }
}

/**
 * 获取字典树形结构选项（用于父级字典选择）
 * @param {string} module - 模块名称（可选）
 * @returns {Promise<Object>} API响应数据
 */
export const getDictTreeOptions = async (module = null) => {
  try {
    const params = module ? { module } : {}
    const response = await axios.get('/api/system/dict/tree-options', { params })
    return response.data
  } catch (error) {
    console.error('获取字典树形结构选项失败:', error)
    throw error
  }
}

/**
 * 获取系统配置分类选项（来自字典 DICT_3 的子项）
 * @returns {Promise<Object>} API响应数据，格式：[{code: "DICT_3.1", label: "系统配置"}, ...]
 */
export const getConfigCategories = async () => {
  try {
    const response = await axios.get('/api/system/dict/config-categories')
    return response.data
  } catch (error) {
    console.error('获取系统配置分类选项失败:', error)
    throw error
  }
}

// 默认导出所有API方法
export default {
  getDictList,
  createDict,
  updateDict,
  deleteDict,
  batchDeleteDict,
  getDictDetail,
  updateDictStatus,
  batchEnableDict,
  batchDisableDict,
  exportSelectedDict,
  exportAllDict,
  importDict,
  refreshDictCache,
  getModules,
  getDictModules,
  getDictTreeOptions,
  getConfigCategories
}
