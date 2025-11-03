/**
 * 个人信息状态管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 20:10:00
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useProfileStore = defineStore('profile', () => {
  // 状态
  const userProfile = ref(null)
  const isLoading = ref(false)
  const isEditing = ref(false)
  const isUploading = ref(false)
  const isChangingPassword = ref(false)

  // 获取器
  const getUserProfile = () => userProfile.value
  const getIsLoading = () => isLoading.value
  const getIsEditing = () => isEditing.value
  const getIsUploading = () => isUploading.value
  const getIsChangingPassword = () => isChangingPassword.value

  // 计算属性
  const hasProfile = computed(() => !!userProfile.value)
  const profileDisplayName = computed(() => {
    if (!userProfile.value) return '未知用户'
    return userProfile.value.nickname || userProfile.value.realName || userProfile.value.username
  })

  // 动作
  const setUserProfile = (profile) => {
    userProfile.value = profile
  }

  const setLoading = (loading) => {
    isLoading.value = loading
  }

  const setEditing = (editing) => {
    isEditing.value = editing
  }

  const setUploading = (uploading) => {
    isUploading.value = uploading
  }

  const setChangingPassword = (changing) => {
    isChangingPassword.value = changing
  }

  // 获取用户个人信息
  const fetchUserProfile = async () => {
    if (isLoading.value) return
    
    try {
      setLoading(true)
      console.log('Profile Store: 开始获取用户个人信息...')
      
      const response = await userApi.getUserProfile()
      console.log('Profile Store: API响应:', response)
      
      if (response.code === 200 && response.data) {
        setUserProfile(response.data)
        console.log('Profile Store: 用户个人信息获取成功:', response.data)
        return { success: true, data: response.data }
      } else {
        console.warn('Profile Store: 获取用户个人信息失败:', response.message)
        setUserProfile(null)
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Profile Store: 获取用户个人信息错误:', error)
      setUserProfile(null)
      return { success: false, message: error.message }
    } finally {
      setLoading(false)
      console.log('Profile Store: 用户个人信息获取完成')
    }
  }

  // 更新用户基本信息
  const updateUserProfile = async (profileData) => {
    try {
      setLoading(true)
      console.log('Profile Store: 开始更新用户个人信息...', profileData)
      
      const response = await userApi.updateUserProfile(profileData)
      console.log('Profile Store: 更新API响应:', response)
      
      if (response.code === 200 && response.data) {
        // 更新本地状态
        setUserProfile({ ...userProfile.value, ...response.data })
        console.log('Profile Store: 用户个人信息更新成功:', response.data)
        
        ElMessage.success('个人信息更新成功')
        return { success: true, data: response.data }
      } else {
        console.warn('Profile Store: 更新用户个人信息失败:', response.message)
        ElMessage.error(response.message || '更新失败')
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Profile Store: 更新用户个人信息错误:', error)
      ElMessage.error('更新失败，请重试')
      return { success: false, message: error.message }
    } finally {
      setLoading(false)
      console.log('Profile Store: 用户个人信息更新完成')
    }
  }

  // 上传用户头像
  const uploadUserAvatar = async (file) => {
    try {
      setUploading(true)
      console.log('Profile Store: 开始上传用户头像...', file)
      
      const formData = new FormData()
      formData.append('avatar', file)
      
      const response = await userApi.uploadUserAvatar(formData)
      console.log('Profile Store: 头像上传API响应:', response)
      
      if (response.code === 200 && response.data) {
        // 更新本地头像
        if (userProfile.value) {
          userProfile.value.avatar = response.data.avatarUrl || response.data.url
        }
        console.log('Profile Store: 用户头像上传成功:', response.data)
        
        ElMessage.success('头像上传成功')
        return { success: true, data: response.data }
      } else {
        console.warn('Profile Store: 上传用户头像失败:', response.message)
        ElMessage.error(response.message || '上传失败')
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Profile Store: 上传用户头像错误:', error)
      ElMessage.error('上传失败，请重试')
      return { success: false, message: error.message }
    } finally {
      setUploading(false)
      console.log('Profile Store: 用户头像上传完成')
    }
  }

  // 修改用户密码
  const changePassword = async (passwordData) => {
    try {
      setChangingPassword(true)
      console.log('Profile Store: 开始修改用户密码...')
      
      const response = await userApi.changePassword(passwordData)
      console.log('Profile Store: 密码修改API响应:', response)
      
      if (response.code === 200) {
        console.log('Profile Store: 用户密码修改成功')
        
        ElMessage.success('密码修改成功')
        return { success: true }
      } else {
        console.warn('Profile Store: 修改用户密码失败:', response.message)
        ElMessage.error(response.message || '密码修改失败')
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Profile Store: 修改用户密码错误:', error)
      ElMessage.error('密码修改失败，请重试')
      return { success: false, message: error.message }
    } finally {
      setChangingPassword(false)
      console.log('Profile Store: 用户密码修改完成')
    }
  }

  // 重置状态
  const resetProfile = () => {
    userProfile.value = null
    isLoading.value = false
    isEditing.value = false
    isUploading.value = false
    isChangingPassword.value = false
  }

  // 初始化个人信息（如果本地没有数据）
  const initializeProfile = async () => {
    if (!hasProfile.value && !isLoading.value) {
      console.log('Profile Store: 初始化个人信息...')
      await fetchUserProfile()
    }
  }

  return {
    // 状态
    userProfile,
    isLoading,
    isEditing,
    isUploading,
    isChangingPassword,
    
    // 获取器
    getUserProfile,
    getIsLoading,
    getIsEditing,
    getIsUploading,
    getIsChangingPassword,
    
    // 计算属性
    hasProfile,
    profileDisplayName,
    
    // 动作
    setUserProfile,
    setLoading,
    setEditing,
    setUploading,
    setChangingPassword,
    fetchUserProfile,
    updateUserProfile,
    uploadUserAvatar,
    changePassword,
    resetProfile,
    initializeProfile
  }
}) 