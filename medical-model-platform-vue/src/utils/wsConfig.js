// 统一管理 WebSocket 基础地址与主题路径，便于一处修改
// 优先读取环境变量，其次回退到开发默认

const WS_BASE = (import.meta && import.meta.env && import.meta.env.VITE_WS_BASE)
  || ((typeof process !== 'undefined') && process.env && (process.env.VUE_APP_WS_BASE || process.env.WS_BASE))
  || 'ws://localhost:8080/api'

export const wsConfig = {
  base: WS_BASE,
  // 主题路径（不含 base）
  topics: {
    roleChanged: '/topic/permission-role-changed',
    userChanged: '/topic/permission-user-changed',
    userMessage: '/topic/user-message',
    userAnnouncement: '/topic/user-announcement'
  },
  // 生成完整 URL
  urlOf(topicPath) {
    if (!topicPath) return null
    // 允许 base 末尾或 topicPath 开头带/，做一次规范化
    const base = WS_BASE.endsWith('/') ? WS_BASE.slice(0, -1) : WS_BASE
    const path = topicPath.startsWith('/') ? topicPath : `/${topicPath}`
    try {
      // eslint-disable-next-line no-console
      console.debug(`[WS] urlOf -> base: ${base}, topic: ${path}, full: ${base}${path}`)
    }
    // eslint-disable-next-line no-empty
    catch (_) {}
    return `${base}${path}`
  }
}

export default wsConfig


