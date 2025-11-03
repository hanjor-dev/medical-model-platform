/**
 * 个人信息工具函数
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 20:10:00
 */

/**
 * 验证头像文件
 * @param {File} file - 头像文件
 * @param {Object} options - 验证选项
 * @param {number} options.maxSize - 最大文件大小（MB）
 * @param {Array} options.allowedTypes - 允许的文件类型
 * @returns {Object} 验证结果 { valid: boolean, message: string }
 */
export const validateAvatarFile = (file, options = {}) => {
  const {
    maxSize = 2, // 默认2MB
    allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  } = options

  // 检查文件类型
  if (!allowedTypes.includes(file.type)) {
    return {
      valid: false,
      message: `只支持 ${allowedTypes.map(type => type.split('/')[1].toUpperCase()).join('、')} 格式的图片`
    }
  }

  // 检查文件大小
  const fileSizeMB = file.size / 1024 / 1024
  if (fileSizeMB > maxSize) {
    return {
      valid: false,
      message: `文件大小不能超过 ${maxSize}MB，当前文件大小：${fileSizeMB.toFixed(2)}MB`
    }
  }

  // 检查文件是否为空
  if (file.size === 0) {
    return {
      valid: false,
      message: '文件不能为空'
    }
  }

  return {
    valid: true,
    message: '文件验证通过'
  }
}

/**
 * 压缩图片文件
 * @param {File} file - 原始图片文件
 * @param {Object} options - 压缩选项
 * @param {number} options.maxWidth - 最大宽度
 * @param {number} options.maxHeight - 最大高度
 * @param {number} options.quality - 压缩质量（0-1）
 * @returns {Promise<File>} 压缩后的文件
 */
export const compressImage = (file, options = {}) => {
  return new Promise((resolve, reject) => {
    const {
      maxWidth = 800,
      maxHeight = 800,
      quality = 0.8
    } = options

    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      // 计算压缩后的尺寸
      let { width, height } = img
      
      if (width > maxWidth || height > maxHeight) {
        const ratio = Math.min(maxWidth / width, maxHeight / height)
        width *= ratio
        height *= ratio
      }

      // 设置canvas尺寸
      canvas.width = width
      canvas.height = height

      // 绘制图片
      ctx.drawImage(img, 0, 0, width, height)

      // 转换为blob
      canvas.toBlob(
        (blob) => {
          if (blob) {
            // 创建新的File对象
            const compressedFile = new File([blob], file.name, {
              type: file.type,
              lastModified: Date.now()
            })
            resolve(compressedFile)
          } else {
            reject(new Error('图片压缩失败'))
          }
        },
        file.type,
        quality
      )
    }

    img.onerror = () => {
      reject(new Error('图片加载失败'))
    }

    // 读取文件
    const reader = new FileReader()
    reader.onload = (e) => {
      img.src = e.target.result
    }
    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }
    reader.readAsDataURL(file)
  })
}

/**
 * 验证用户基本信息表单
 * @param {Object} data - 表单数据
 * @returns {Object} 验证结果 { valid: boolean, errors: Object }
 */
export const validateBasicInfo = (data) => {
  const errors = {}

  // 昵称验证
  if (!data.nickname || data.nickname.trim() === '') {
    errors.nickname = '昵称不能为空'
  } else if (data.nickname.length < 2 || data.nickname.length > 20) {
    errors.nickname = '昵称长度必须在2-20个字符之间'
  }

  // 真实姓名验证
  if (!data.realName || data.realName.trim() === '') {
    errors.realName = '真实姓名不能为空'
  } else if (data.realName.length < 2 || data.realName.length > 20) {
    errors.realName = '真实姓名长度必须在2-20个字符之间'
  }

  // 邮箱验证
  if (!data.email || data.email.trim() === '') {
    errors.email = '邮箱不能为空'
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(data.email)) {
      errors.email = '请输入正确的邮箱格式'
    }
  }

  // 手机号验证（可选）
  if (data.phone && data.phone.trim() !== '') {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(data.phone)) {
      errors.phone = '请输入正确的手机号码'
    }
  }

  // 个人简介验证（可选）
  if (data.bio && data.bio.length > 200) {
    errors.bio = '个人简介不能超过200个字符'
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors
  }
}

/**
 * 验证密码修改表单
 * @param {Object} data - 表单数据
 * @returns {Object} 验证结果 { valid: boolean, errors: Object }
 */
export const validatePasswordChange = (data) => {
  const errors = {}

  // 当前密码验证
  if (!data.currentPassword || data.currentPassword.trim() === '') {
    errors.currentPassword = '请输入当前密码'
  }

  // 新密码验证
  if (!data.newPassword || data.newPassword.trim() === '') {
    errors.newPassword = '请输入新密码'
  } else if (data.newPassword.length < 6) {
    errors.newPassword = '新密码长度不能少于6个字符'
  } else if (data.newPassword.length > 20) {
    errors.newPassword = '新密码长度不能超过20个字符'
  } else {
    // 密码强度验证
    const hasLower = /[a-z]/.test(data.newPassword)
    const hasUpper = /[A-Z]/.test(data.newPassword)
    const hasNumber = /\d/.test(data.newPassword)

    if (!hasLower || !hasUpper || !hasNumber) {
      errors.newPassword = '密码必须包含大小写字母和数字'
    }
  }

  // 确认密码验证
  if (!data.confirmPassword || data.confirmPassword.trim() === '') {
    errors.confirmPassword = '请再次输入新密码'
  } else if (data.confirmPassword !== data.newPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  // 新密码不能与当前密码相同
  if (data.currentPassword && data.newPassword && data.currentPassword === data.newPassword) {
    errors.newPassword = '新密码不能与当前密码相同'
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 生成头像预览URL
 * @param {File} file - 头像文件
 * @returns {Promise<string>} 预览URL
 */
export const generateAvatarPreview = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    
    reader.onload = (e) => {
      resolve(e.target.result)
    }
    
    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }
    
    reader.readAsDataURL(file)
  })
}

/**
 * 清理表单数据
 * @param {Object} data - 原始数据
 * @returns {Object} 清理后的数据
 */
export const sanitizeFormData = (data) => {
  const cleaned = {}
  
  Object.keys(data).forEach(key => {
    if (data[key] !== null && data[key] !== undefined) {
      if (typeof data[key] === 'string') {
        cleaned[key] = data[key].trim()
      } else {
        cleaned[key] = data[key]
      }
    }
  })
  
  return cleaned
}

/**
 * 获取密码强度等级
 * @param {string} password - 密码
 * @returns {Object} 密码强度信息 { level: number, text: string, color: string }
 */
export const getPasswordStrength = (password) => {
  if (!password) {
    return { level: 0, text: '无', color: '#d9d9d9' }
  }

  let score = 0

  // 长度检查
  if (password.length >= 8) score += 1
  if (password.length >= 12) score += 1

  // 字符类型检查
  if (/[a-z]/.test(password)) score += 1
  if (/[A-Z]/.test(password)) score += 1
  if (/\d/.test(password)) score += 1
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 1

  // 复杂度检查
  if (password.length >= 8 && /[a-z]/.test(password) && /[A-Z]/.test(password) && /\d/.test(password)) {
    score += 1
  }

  // 返回强度等级
  if (score <= 2) {
    return { level: score, text: '弱', color: '#ff4d4f' }
  } else if (score <= 4) {
    return { level: score, text: '中', color: '#faad14' }
  } else if (score <= 6) {
    return { level: score, text: '强', color: '#52c41a' }
  } else {
    return { level: score, text: '很强', color: '#1890ff' }
  }
} 