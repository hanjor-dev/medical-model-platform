import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 使用相对路径，通过代理转发到后端
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加认证token
    const authStore = useAuthStore()
    const token = authStore.getToken()
    
    // 检查token是否已过期
    if (token && authStore.isTokenExpired()) {
      console.log('检测到token已过期，清除本地状态')
      // token已过期，直接清除本地状态
      localStorage.removeItem('token')
      localStorage.removeItem('user-permissions')
      localStorage.removeItem('user-role')
      localStorage.removeItem('user-menu-permissions')
      
      // 不添加Authorization头，让请求自然失败
      console.warn('token已过期，请求将失败')
    } else if (token) {
      // 直接使用后端返回的原始token值，不添加任何前缀
      config.headers.Authorization = token
    }
    
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const { data } = response

    // 统一返回结构：{ code, success, message, data }
    // 兼容多种后端返回：
    // 1) { code: 200, message, data }
    // 2) { success: true, message, data }
    // 3) 直接返回数据对象
    if (data && typeof data === 'object') {
      const hasCode = Object.prototype.hasOwnProperty.call(data, 'code')
      const hasSuccess = Object.prototype.hasOwnProperty.call(data, 'success')

      if (hasCode || hasSuccess) {
        const numericCode = hasCode
          ? (typeof data.code === 'string' ? parseInt(data.code, 10) : data.code)
          : (data.success === true ? 200 : 500)

        const normalized = {
          code: Number.isFinite(numericCode) ? numericCode : 500,
          success: hasSuccess ? !!data.success : (numericCode === 200),
          message: data.message || (hasSuccess ? (data.success ? '请求成功' : '请求失败') : '请求成功'),
          data: data.data !== undefined ? data.data : null
        }
        return normalized
      }

      // 非标准，包装为成功
      return {
        code: 200,
        success: true,
        message: '请求成功',
        data
      }
    }

    // 原始类型（字符串/数字等）也包装
    return {
      code: 200,
      success: true,
      message: '请求成功',
      data
    }
  },
  error => {
    console.error('响应拦截器错误:', error)
    
    let message = '网络错误，请重试'
    let code = 500
    
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response
      code = status
      
      if (status === 401) {
        // 未授权，直接清除本地状态，不调用logout接口
        console.log('检测到401错误，直接清除本地认证状态')
        
        // 直接清除localStorage中的token和权限数据
        localStorage.removeItem('token')
        localStorage.removeItem('user-permissions')
        localStorage.removeItem('user-role')
        localStorage.removeItem('user-menu-permissions')
        
        // 显示消息
        message = '登录已过期，请重新登录'
        
        // 使用router跳转而不是window.location，避免页面刷新
        // 注意：这里不能直接使用router，需要通过其他方式
        setTimeout(() => {
          // 延迟跳转，避免在错误处理过程中跳转
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
        }, 100)
        
      } else if (status === 403) {
        message = '权限不足，无法访问'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 500) {
        message = '服务器错误，请稍后重试'
      } else if (data && data.message) {
        message = data.message
      } else {
        message = `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '服务器无响应，请检查网络连接'
    } else if (error.message) {
      // 其他错误
      message = error.message
    }
    
    // 显示错误消息
    if (window.ElMessage) {
      window.ElMessage.error(message)
    } else {
      console.error(message)
    }
    
    // 返回统一的错误格式
    return Promise.reject({
      success: false,
      code: code,
      message: message,
      data: null
    })
  }
)

// 封装请求方法
const request = (config) => {
  return service(config)
}

// 导出
export default request
export { service as axios } 