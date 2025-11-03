/**
 * 异常处理工具
 * 统一处理各种异常情况，包括API错误、验证错误、网络错误等
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 21:30:00
 */

import { ElMessage } from 'element-plus'

/**
 * 异常类型枚举
 */
export const ExceptionType = {
  VALIDATION: 'validation',      // 验证错误
  API: 'api',                    // API错误
  NETWORK: 'network',            // 网络错误
  AUTH: 'auth',                  // 认证错误
  PERMISSION: 'permission',      // 权限错误
  BUSINESS: 'business',          // 业务错误
  SYSTEM: 'system'               // 系统错误
}

/**
 * 异常处理配置
 */
const exceptionConfig = {
  // 是否显示错误消息
  showMessage: true,
  // 是否记录错误日志
  logError: true,
  // 错误消息显示时长
  messageDuration: 5000,
  // 是否自动重试
  autoRetry: false,
  // 重试次数
  maxRetryCount: 3
}

/**
 * 设置异常处理配置
 * @param {Object} config - 配置对象
 */
export const setExceptionConfig = (config) => {
  Object.assign(exceptionConfig, config)
}

/**
 * 记录错误日志
 * @param {Error|Object} error - 错误对象
 * @param {string} context - 错误上下文
 */
const logError = (error, context = '') => {
  if (!exceptionConfig.logError) return

  const errorInfo = {
    timestamp: new Date().toISOString(),
    context,
    type: error.type || 'unknown',
    message: error.message || '未知错误',
    stack: error.stack,
    data: error.data || error.response?.data
  }

  console.error('🚨 异常处理:', errorInfo)
  
  // 在生产环境中，可以将错误信息发送到日志服务
  if (process.env.NODE_ENV === 'production') {
    // TODO: 发送错误日志到服务器
    // sendErrorLog(errorInfo)
  }
}

/**
 * 显示错误消息
 * @param {string} message - 错误消息
 * @param {string} type - 消息类型
 * @param {number} duration - 显示时长
 */
const showMessage = (message, type = 'error', duration = null) => {
  if (!exceptionConfig.showMessage) return

  const messageDuration = duration || exceptionConfig.messageDuration
  
  if (window.$message) {
    // 使用自定义消息组件
    window.$message[type](message, '', messageDuration)
  } else {
    // 使用Element Plus消息
    ElMessage({
      message,
      type,
      duration: messageDuration
    })
  }
}

/**
 * 处理验证错误
 * @param {Object} validationErrors - 验证错误对象
 * @param {string} context - 错误上下文
 */
export const handleValidationError = (validationErrors, context = '表单验证') => {
  const error = {
    type: ExceptionType.VALIDATION,
    message: '表单验证失败',
    data: validationErrors,
    context
  }

  logError(error, context)
  
  // 显示第一个验证错误
  if (validationErrors && typeof validationErrors === 'object') {
    const firstError = Object.values(validationErrors)[0]
    if (firstError) {
      showMessage(firstError, 'warning')
    }
  } else {
    showMessage('表单验证失败，请检查输入信息', 'warning')
  }

  return error
}

/**
 * 处理API错误
 * @param {Error|Object} apiError - API错误对象
 * @param {string} context - 错误上下文
 */
export const handleApiError = (apiError, context = 'API请求') => {
  let error = {
    type: ExceptionType.API,
    message: 'API请求失败',
    data: apiError,
    context
  }

  // 处理不同类型的API错误
  if (apiError.response) {
    const { status, data } = apiError.response
    
    switch (status) {
      case 400:
        error.message = data?.message || '请求参数错误'
        error.type = ExceptionType.VALIDATION
        break
      case 401:
        error.message = '登录已过期，请重新登录'
        error.type = ExceptionType.AUTH
        // 自动跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 2000)
        break
      case 403:
        error.message = '权限不足，无法访问'
        error.type = ExceptionType.PERMISSION
        break
      case 404:
        error.message = '请求的资源不存在'
        error.type = ExceptionType.API
        break
      case 500:
        error.message = '服务器内部错误'
        error.type = ExceptionType.SYSTEM
        break
      default:
        error.message = data?.message || `请求失败 (${status})`
    }
  } else if (apiError.request) {
    error.message = '服务器无响应，请检查网络连接'
    error.type = ExceptionType.NETWORK
  } else if (apiError.message) {
    error.message = apiError.message
  }

  logError(error, context)
  showMessage(error.message, 'error')
  
  return error
}

/**
 * 处理网络错误
 * @param {Error} networkError - 网络错误对象
 * @param {string} context - 错误上下文
 */
export const handleNetworkError = (networkError, context = '网络请求') => {
  const error = {
    type: ExceptionType.NETWORK,
    message: '网络连接失败，请检查网络设置',
    data: networkError,
    context
  }

  logError(error, context)
  showMessage(error.message, 'error')
  
  return error
}

/**
 * 处理认证错误
 * @param {Error} authError - 认证错误对象
 * @param {string} context - 错误上下文
 */
export const handleAuthError = (authError, context = '用户认证') => {
  const error = {
    type: ExceptionType.AUTH,
    message: '认证失败，请重新登录',
    data: authError,
    context
  }

  logError(error, context)
  showMessage(error.message, 'error')
  
  // 自动跳转到登录页
  setTimeout(() => {
    window.location.href = '/login'
  }, 2000)
  
  return error
}

/**
 * 处理权限错误
 * @param {Error} permissionError - 权限错误对象
 * @param {string} context - 错误上下文
 */
export const handlePermissionError = (permissionError, context = '权限检查') => {
  const error = {
    type: ExceptionType.PERMISSION,
    message: '权限不足，无法执行此操作',
    data: permissionError,
    context
  }

  logError(error, context)
  showMessage(error.message, 'warning')
  
  return error
}

/**
 * 处理业务错误
 * @param {Error|Object} businessError - 业务错误对象
 * @param {string} context - 错误上下文
 */
export const handleBusinessError = (businessError, context = '业务逻辑') => {
  const error = {
    type: ExceptionType.BUSINESS,
    message: businessError.message || '业务操作失败',
    data: businessError,
    context
  }

  logError(error, context)
  showMessage(error.message, 'warning')
  
  return error
}

/**
 * 处理系统错误
 * @param {Error} systemError - 系统错误对象
 * @param {string} context - 错误上下文
 */
export const handleSystemError = (systemError, context = '系统错误') => {
  const error = {
    type: ExceptionType.SYSTEM,
    message: '系统发生错误，请稍后重试',
    data: systemError,
    context
  }

  logError(error, context)
  showMessage(error.message, 'error')
  
  return error
}

/**
 * 通用异常处理器
 * @param {Error|Object} error - 错误对象
 * @param {string} context - 错误上下文
 */
export const handleException = (error, context = '未知操作') => {
  // 根据错误类型分发到对应的处理器
  switch (error.type) {
    case ExceptionType.VALIDATION:
      return handleValidationError(error, context)
    case ExceptionType.API:
      return handleApiError(error, context)
    case ExceptionType.NETWORK:
      return handleNetworkError(error, context)
    case ExceptionType.AUTH:
      return handleAuthError(error, context)
    case ExceptionType.PERMISSION:
      return handlePermissionError(error, context)
    case ExceptionType.BUSINESS:
      return handleBusinessError(error, context)
    case ExceptionType.SYSTEM:
      return handleSystemError(error, context)
    default:
      // 尝试自动识别错误类型
      if (error.response) {
        return handleApiError(error, context)
      } else if (error.request) {
        return handleNetworkError(error, context)
      } else {
        return handleSystemError(error, context)
      }
  }
}

/**
 * 创建异常对象
 * @param {string} type - 异常类型
 * @param {string} message - 异常消息
 * @param {Object} data - 异常数据
 * @returns {Object} 异常对象
 */
export const createException = (type, message, data = {}) => {
  return {
    type,
    message,
    data,
    timestamp: new Date().toISOString()
  }
}

/**
 * 异步操作异常包装器
 * @param {Function} asyncFn - 异步函数
 * @param {string} context - 操作上下文
 * @returns {Promise} 包装后的Promise
 */
export const withExceptionHandler = (asyncFn, context = '异步操作') => {
  return async (...args) => {
    try {
      return await asyncFn(...args)
    } catch (error) {
      handleException(error, context)
      throw error
    }
  }
}

export default {
  ExceptionType,
  setExceptionConfig,
  handleValidationError,
  handleApiError,
  handleNetworkError,
  handleAuthError,
  handlePermissionError,
  handleBusinessError,
  handleSystemError,
  handleException,
  createException,
  withExceptionHandler
} 