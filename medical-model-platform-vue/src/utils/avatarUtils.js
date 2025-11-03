/**
 * 头像工具函数
 * 用于处理用户头像的显示和默认头像生成
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

/**
 * 生成默认头像的SVG内容
 * @param {string} text - 显示的文字（通常是用户名首字母或首字）
 * @returns {string} SVG字符串
 */
export const generateDefaultAvatar = (text) => {
  // 使用更柔和的蓝色背景，与整体设计系统协调
  const backgroundColor = '#4096ff'
  
  // 计算文字大小，根据文字长度调整，适配32x32尺寸
  let fontSize = 16
  if (text.length > 1) {
    fontSize = Math.max(12, 16 - (text.length - 1) * 2)
  }
  
  // 生成优化的SVG头像，添加微妙的径向渐变
  const svg = `
    <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" style="overflow: visible;">
      <defs>
        <radialGradient id="avatarGradient" cx="30%" cy="30%" r="70%">
          <stop offset="0%" style="stop-color:${backgroundColor};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${backgroundColor};stop-opacity:0.85" />
        </radialGradient>
      </defs>
      <circle cx="16" cy="16" r="16" fill="url(#avatarGradient)" stroke="none" stroke-width="0"/>
      <text x="16" y="16.5" font-family="Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif" 
            font-size="${fontSize}" font-weight="600" text-anchor="middle" fill="white" 
            dominant-baseline="central">
        ${text}
      </text>
    </svg>
  `
  
  return svg
}

/**
 * 获取用户头像
 * @param {Object} user - 用户对象
 * @returns {Object} 头像信息 { type: 'image'|'svg', content: string, alt: string }
 */
export const getUserAvatar = (user) => {
  if (!user) {
    return {
      type: 'svg',
      content: generateDefaultAvatar('?', ''),
      alt: '未知用户'
    }
  }
  
  // 如果有头像URL，优先使用
  if (user.avatar && user.avatar.trim()) {
    return {
      type: 'image',
      content: user.avatar,
      alt: user.username || user.nickname || '用户头像'
    }
  }
  
  // 如果没有头像，生成默认头像
  let displayText = '?'
  
  if (user.username) {
    // 优先使用用户名首字母
    displayText = user.username.charAt(0).toUpperCase()
  } else if (user.nickname) {
    // 如果没有用户名，使用昵称首字
    displayText = user.nickname.charAt(0)
  } else if (user.realName) {
    // 如果没有昵称，使用真实姓名首字
    displayText = user.realName.charAt(0)
  }
  
  return {
    type: 'svg',
    content: generateDefaultAvatar(displayText, user.username || user.nickname || user.realName || ''),
    alt: `${displayText}的头像`
  }
}

/**
 * 获取用户显示名称
 * @param {Object} user - 用户对象
 * @returns {string} 显示名称
 */
export const getUserDisplayName = (user) => {
  if (!user) return '未知用户'
  
  return user.nickname || user.realName || user.username || '未知用户'
}

/**
 * 验证头像URL是否有效
 * @param {string} url - 头像URL
 * @returns {boolean} 是否有效
 */
export const isValidAvatarUrl = (url) => {
  if (!url || typeof url !== 'string') return false
  
  // 检查是否是有效的URL格式
  try {
    const urlObj = new URL(url)
    return ['http:', 'https:'].includes(urlObj.protocol)
  } catch {
    return false
  }
} 