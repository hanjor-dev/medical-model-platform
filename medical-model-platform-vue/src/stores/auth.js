import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import { myCreditsApi } from '@/api/credit-system'
import { usePermissionStore } from './permission'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  // 从 localStorage 恢复用户信息，供 WebSocket 等场景在刷新后可立即读取 userId
  let initialUser = null
  try { initialUser = JSON.parse(localStorage.getItem('user') || 'null') } catch (_) { initialUser = null }
  const user = ref(initialUser)
  const token = ref(localStorage.getItem('token') || '')
  const isAuthenticated = ref(!!token.value)
  
  // 用户信息缓存相关
  const userInfoCache = ref({
    lastFetchTime: 0,
    cacheExpiry: 5 * 60 * 1000, // 5分钟缓存过期时间
    isFetching: false
  })

  // 获取器
  const getUser = () => user.value
  const getToken = () => token.value
  const getIsAuthenticated = () => isAuthenticated.value
  
  // 检查用户信息缓存是否有效
  const isUserInfoCacheValid = () => {
    if (!userInfoCache.value.lastFetchTime) return false
    
    const now = Date.now()
    const timeSinceLastFetch = now - userInfoCache.value.lastFetchTime
    
    return timeSinceLastFetch < userInfoCache.value.cacheExpiry
  }
  
  // 检查是否需要刷新用户信息
  const needsUserInfoRefresh = () => {
    // 如果没有用户信息，需要获取
    if (!user.value) return true
    
    // 如果缓存过期，需要刷新
    if (!isUserInfoCacheValid()) return true
    
    // 如果用户信息不完整，需要刷新
    const hasCompleteInfo = user.value.email || user.value.phone || user.value.avatar
    if (!hasCompleteInfo) return true
    
    return false
  }

  // 动作
  const setToken = (newToken) => {
    token.value = newToken
    isAuthenticated.value = !!newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      try { localStorage.setItem('user', JSON.stringify(userData)) } catch (_) { /* noop */ }
    } else {
      localStorage.removeItem('user')
    }
  }

  // WebSocket 权限监听已取消，采用接口成功后主动刷新

  // 积分数据状态 - 直接暴露给组件使用
  const creditsData = ref(null)
  const isLoadingCredits = ref(false)

  // 更新积分数据
  const updateUserCredits = (credits) => {
    if (credits && credits.accounts && Array.isArray(credits.accounts)) {
      creditsData.value = credits
      console.log('Auth Store: 积分数据更新成功，账户数量:', credits.accounts.length)
    } else {
      console.warn('Auth Store: 积分数据格式无效:', credits)
      creditsData.value = null
    }
  }

  // 获取积分数据 - 保持向后兼容
  const getUserCredits = () => {
    return creditsData.value
  }

  // 获取积分数据状态 - 保持向后兼容
  const getCreditsLoadingState = () => {
    return isLoadingCredits.value
  }

  // 设置积分数据加载状态
  const setCreditsLoadingState = (loading) => {
    isLoadingCredits.value = loading
  }

  // 获取用户积分数据
  const fetchUserCredits = async () => {
    if (isLoadingCredits.value) return
    
    try {
      setCreditsLoadingState(true)
      console.log('开始获取用户积分数据...')
      console.log('Auth Store: 开始获取积分数据...')
      
      const response = await myCreditsApi.getMyCreditsBalance()
      console.log('Auth Store: API响应:', response)
      
      if (response.code === 200 && response.data) {
        updateUserCredits(response.data)
        console.log('Auth Store: 积分数据更新成功:', response.data)
        return { success: true, data: response.data }
      } else {
        console.warn('Auth Store: 获取积分数据失败:', response.message)
        updateUserCredits(null)
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Auth Store: 获取积分数据错误:', error)
      updateUserCredits(null)
      return { success: false, message: error.message }
    } finally {
      setCreditsLoadingState(false)
      console.log('Auth Store: 积分数据获取完成')
    }
  }

  // 检查token是否即将过期（提前5分钟）
  const isTokenExpiringSoon = () => {
    if (!token.value) return true
    
    try {
      // 如果token是JWT格式，尝试解析过期时间
      const tokenParts = token.value.split('.')
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]))
        if (payload.exp) {
          const currentTime = Math.floor(Date.now() / 1000)
          const timeUntilExpiry = payload.exp - currentTime
          // 如果token在5分钟内过期，认为即将过期
          return timeUntilExpiry <= 300
        }
      }
    } catch (error) {
      console.warn('无法解析token过期时间:', error)
    }
    
    return false
  }
  
  // 检查token是否已过期
  const isTokenExpired = () => {
    if (!token.value) return true
    
    try {
      // 如果token是JWT格式，尝试解析过期时间
      const tokenParts = token.value.split('.')
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]))
        if (payload.exp) {
          const currentTime = Math.floor(Date.now() / 1000)
          return currentTime >= payload.exp
        }
      }
    } catch (error) {
      console.warn('无法解析token过期时间:', error)
    }
    
    return false
  }

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials)
      
      // 根据后端接口文档，成功时code为200
      if (response.code === 200) {
        const { token: authToken, userId, username, role, permissions } = response.data
        
        // 设置token和用户信息
        setToken(authToken)
        setUser({
          userId,
          username,
          nickname: response.data.nickname, // 添加昵称字段
          role,
          permissions,
          currentTeamId: response.data.currentTeamId,
          currentTeamName: response.data.currentTeamName
        })
        
        // 设置权限信息到权限Store
        const permissionStore = usePermissionStore()
        permissionStore.setPermissions({
          role,
          permissions
        })
        
        // 登录成功后自动获取积分数据
        console.log('登录成功，开始获取积分数据...')
        fetchUserCredits()
        // 实时权限监听已移除；依赖业务操作成功回调后主动刷新
        
        return { success: true, message: response.message || '登录成功' }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录错误:', error)
      return { 
        success: false, 
        message: error.message || '登录失败，请重试' 
      }
    }
  }

  const register = async (userData) => {
    try {
      const response = await authApi.register(userData)
      
      // 根据后端接口文档，成功时code为200
      if (response.code === 200) {
        return { success: true, message: response.message || '注册成功' }
      } else {
        return { success: false, message: response.message || '注册失败' }
      }
    } catch (error) {
      console.error('注册错误:', error)
      return { 
        success: false, 
        message: error.message || '注册失败，请重试' 
      }
    }
  }

  const logout = async (callBackend = true) => {
    try {
      // 只有在明确要求且token有效时才调用后端登出接口
      if (callBackend && token.value) {
        try {
          await authApi.logout()
          console.log('后端登出接口调用成功')
        } catch (error) {
          console.warn('后端登出接口调用失败，继续清除本地状态:', error)
        }
      }
    } catch (error) {
      console.error('登出错误:', error)
    } finally {
      // 无论后端是否成功，都清除本地状态
      console.log('清除本地认证状态')
      setToken('')
      setUser(null)
      
      // 清除权限信息
      const permissionStore = usePermissionStore()
      permissionStore.clearPermissions() // 这会同时清除localStorage
      
      // 实时权限监听已移除
    }
  }

  const checkAuth = async () => {
    if (!token.value) {
      console.log('checkAuth: 没有token')
      return false
    }

    // 检查缓存是否有效，如果有效则直接返回
    if (isUserInfoCacheValid() && user.value) {
      console.log('checkAuth: 使用缓存用户信息，跳过API调用')
      return true
    }

    // 如果正在获取中，等待完成
    if (userInfoCache.value.isFetching) {
      console.log('checkAuth: 用户信息正在获取中，等待完成...')
      // 等待获取完成
      while (userInfoCache.value.isFetching) {
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      return !!user.value
    }

    try {
      userInfoCache.value.isFetching = true
      console.log('checkAuth: 开始验证token...')
      
      const response = await authApi.getCurrentUser()
      console.log('checkAuth: 后端响应:', response)
      
      if (response.code === 200) {
        const userData = response.data
        console.log('checkAuth: 设置用户数据:', userData)
        setUser(userData)
        
        // 更新缓存时间
        userInfoCache.value.lastFetchTime = Date.now()
        
        // 更新权限信息
        if (userData.role && userData.permissions) {
          const permissionStore = usePermissionStore()
          console.log('checkAuth: 设置权限信息:', {
            role: userData.role,
            permissionsCount: userData.permissions.length
          })
          permissionStore.setPermissions({
            role: userData.role,
            permissions: userData.permissions
          })
          
          // 等待权限数据完全设置完成
          await new Promise(resolve => setTimeout(resolve, 50))
          
          console.log('checkAuth: 权限设置完成，验证权限数据:', {
            userPermissions: permissionStore.getUserPermissions().length,
            menuPermissions: permissionStore.getMenuPermissions().length
          })
        }
        
        // 认证成功后获取积分数据
        console.log('checkAuth: 认证成功，开始获取积分数据...')
        fetchUserCredits()
        
        // 实时权限已移除
        
        console.log('checkAuth: 认证成功')
        return true
      } else {
        // token无效，清除认证状态
        console.log('checkAuth: token无效，清除认证状态')
        logout()
        return false
      }
    } catch (error) {
      console.error('checkAuth: 认证检查错误:', error)
      logout()
      return false
    } finally {
      userInfoCache.value.isFetching = false
    }
  }

  // 移除自动调用checkAuth，改为在需要时手动调用
  // if (token.value) {
  //   checkAuth()
  // }

  // 智能获取用户信息（优先使用缓存，必要时才调用API）
  const getOrFetchUserInfo = async (forceRefresh = false) => {
    // 如果强制刷新或需要刷新，则调用API
    if (forceRefresh || needsUserInfoRefresh()) {
      console.log('getOrFetchUserInfo: 需要刷新用户信息')
      await checkAuth()
      return user.value
    }
    
    // 否则直接返回缓存的用户信息
    console.log('getOrFetchUserInfo: 使用缓存用户信息')
    return user.value
  }
  
  // 刷新用户信息缓存
  const refreshUserInfo = async () => {
    console.log('refreshUserInfo: 强制刷新用户信息')
    userInfoCache.value.lastFetchTime = 0 // 清除缓存时间
    return await getOrFetchUserInfo(true)
  }

  return {
    // 状态
    user,
    token,
    isAuthenticated,
    creditsData,        // 直接暴露积分数据
    isLoadingCredits,   // 直接暴露加载状态
    
    // 获取器
    getUser,
    getToken,
    getIsAuthenticated,
    
    // 动作
    setToken,
    setUser,
    updateUserCredits,
    getUserCredits,
    getCreditsLoadingState,
    setCreditsLoadingState,
    fetchUserCredits,
    login,
    register,
    logout,
    checkAuth,
    
    // token检查方法
    isTokenExpiringSoon,
    isTokenExpired,

    // 用户信息缓存方法
    isUserInfoCacheValid,
    needsUserInfoRefresh,

    // 智能获取用户信息方法
    getOrFetchUserInfo,
    refreshUserInfo
  }
}) 