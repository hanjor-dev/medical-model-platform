import { ref, computed } from 'vue'
import { createNotificationWsClient } from '@/utils/realtimeWsClient'
import { useAuthStore } from '@/stores/auth'
import { messageApi } from '@/api/notify/message'
import { announcementApi } from '@/api/notify/announcement'

// 通知状态管理
const notifications = ref([])
const announcements = ref([])
const unreadMessageCount = ref(0)
const unreadAnnouncementCount = ref(0)
const currentAnnouncement = ref(null)
const showAnnouncementPopup = ref(false)

let notificationWs = null

// 计算属性
const totalUnreadCount = computed(() => unreadMessageCount.value + unreadAnnouncementCount.value)

// 获取消息列表
const fetchMessages = async () => {
  try {
    console.info('[WS][notify] fetchMessages -> request')
    // 普通用户使用用户端接口
    const response = await messageApi.getMyList({
      pageNum: 1,
      pageSize: 20
    })
    if (response.code === 200) {
      notifications.value = response.data.records || []
      const backendUnread = response.data?.unreadTotal ?? response.data?.unreadCount
      unreadMessageCount.value = (typeof backendUnread === 'number')
        ? backendUnread
        : notifications.value.filter(n => !n.isRead).length
      try { console.info('[WS][notify] fetchMessages -> success', { total: notifications.value.length, unread: unreadMessageCount.value }) } catch (_) { void 0 }
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

// 获取公告列表（仅未读可见，用于上线补拉）
const fetchAnnouncements = async () => {
  try {
    console.info('[WS][notify] fetchAnnouncements -> request')
    const response = await announcementApi.getVisibleUnread({
      pageNum: 1,
      pageSize: 10
    })
    if (response.code === 200) {
      announcements.value = response.data.records || []
      // 接口已返回未读集合，这里直接计数
      unreadAnnouncementCount.value = (announcements.value || []).length
      try { console.info('[WS][notify] fetchAnnouncements -> success', { total: announcements.value.length, unread: unreadAnnouncementCount.value }) } catch (_) { void 0 }
      // 登录/刷新补拉：若存在未读且当前未展示弹窗，则自动弹出第一条
      if (unreadAnnouncementCount.value > 0 && !showAnnouncementPopup.value && !currentAnnouncement.value) {
        const first = announcements.value[0]
        if (first) {
          showAnnouncement({
            id: first.id,
            title: first.title || '系统公告',
            content: first.content || '',
            priority: typeof first.priority === 'number' ? first.priority : 2,
            forceRead: !!first.forceRead,
            createTime: first.createTime || first.updateTime || new Date().toISOString()
          })
          try { console.info('[WS][notify] auto popup unread announcement') } catch (_) { void 0 }
        }
      }
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
  }
}

// 标记消息为已读（单条）并从后端刷新
const markMessageAsRead = async (messageId) => {
  try {
    try { console.info('[WS][notify] markMessageAsRead ->', { messageId }) } catch (_) { void 0 }
    await messageApi.markRead(messageId)
    await fetchMessages()
  } catch (error) {
    console.error('标记消息已读失败:', error)
    throw error
  }
}

// 全部消息设为已读并从后端刷新
const markAllMessagesAsRead = async () => {
  try {
    try { console.info('[WS][notify] markAllMessagesAsRead') } catch (_) { void 0 }
    await messageApi.markAllRead()
    await fetchMessages()
  } catch (error) {
    console.error('全部设为已读失败:', error)
    throw error
  }
}

// 标记公告为已读
const markAnnouncementAsRead = async (announcementId) => {
  try {
    try { console.info('[WS][notify] markAnnouncementAsRead ->', { announcementId }) } catch (_) { void 0 }
    await announcementApi.read(announcementId)
    // 更新本地状态
    announcements.value.forEach(a => {
      if (a.id === announcementId) {
        a.isRead = true
      }
    })
    unreadAnnouncementCount.value = announcements.value.filter(a => !a.isRead).length
  } catch (error) {
    console.error('标记公告已读失败:', error)
    throw error
  }
}

// 显示公告弹窗
const showAnnouncement = (announcement) => {
  currentAnnouncement.value = announcement
  showAnnouncementPopup.value = true
}

// 关闭公告弹窗
const closeAnnouncementPopup = () => {
  showAnnouncementPopup.value = false
  currentAnnouncement.value = null
}

// WebSocket 消息处理（移除消息弹窗，仅刷新列表）
const handleNewMessage = (data) => {
  try { console.debug('[WS][notify] incoming message event <-', data) } catch (_) { void 0 }
  if (data && data.event === 'NEW_MESSAGE' && data.userId) {
    // 只在当前用户匹配时刷新
    try {
      const me = JSON.parse(localStorage.getItem('user') || 'null')
      if (me && Number(data.userId) === Number(me.userId)) {
        try { console.info('[WS][notify] NEW_MESSAGE match current user -> refresh') } catch (_) { void 0 }
        fetchMessages()
      }
    } catch (_) {
      // 回退：仍然刷新，避免漏消息
      try { console.info('[WS][notify] NEW_MESSAGE fallback refresh') } catch (_) { void 0 }
      fetchMessages()
    }
  }
}

const handleNewAnnouncement = (data) => {
  try { console.debug('[WS][notify] incoming announcement event <-', data) } catch (_) { void 0 }
  if (data && data.event === 'NEW_ANNOUNCEMENT') {
    if (data.title || data.content) {
      showAnnouncement({
        id: data.announcementId || data.id,
        title: data.title || '系统公告',
        content: data.content || '',
        priority: data.priority || 2,
        forceRead: !!data.forceRead,
        createTime: data.createTime || new Date().toISOString()
      })
      try { console.info('[WS][notify] showAnnouncement popup') } catch (_) { void 0 }
      fetchAnnouncements()
      return
    }
    fetchAnnouncements()
  }
}

// 初始化 WebSocket 连接（公告为全量广播，消息按需携带 userId）
const initWebSocket = () => {
  if (notificationWs) {
    try { console.info('[WS][notify] initWebSocket -> already connected, skip') } catch (_) { void 0 }
    return
  }
  
  // 优先从 auth store 获取，回退读取 localStorage，避免刷新后 user 丢失
  let uid
  try {
    const auth = useAuthStore()
    // Pinia setup store 属性已解包，不需要 .value
    uid = (auth && auth.user && auth.user.userId) || (auth && auth.getUser && auth.getUser()?.userId)
  } catch (_) { /* 可能还未创建 pinia 实例，继续回退 */ void 0 }
  
  let me
  if (!uid) {
    try { me = JSON.parse(localStorage.getItem('user') || 'null') } catch (_) { me = null; void 0 }
    uid = me && me.userId ? me.userId : undefined
  }
  
  try { console.info('[WS][notify] initWebSocket -> starting with userId (optional):', uid) } catch (_) { void 0 }
  notificationWs = createNotificationWsClient({
    onNewMessage: handleNewMessage,
    onNewAnnouncement: handleNewAnnouncement,
    userId: uid
  })
  try { console.info('[WS][notify] notificationWs.start()') } catch (_) { void 0 }
  notificationWs.start()

  // 若此时还没有 userId，等用户信息就绪后自动重建带 userId 的消息通道，避免缺少 userId 导致连接被后端拒绝
  if (!uid) {
    try {
      const auth = useAuthStore()
      // 轮询等待 userId 出现（最多 10 秒，每 500ms 检查一次）
      let waited = 0
      const timer = setInterval(() => {
        const u = (auth && auth.getUser && auth.getUser()) || null
        if (u && u.userId) {
          clearInterval(timer)
          try { console.info('[WS][notify] userId ready, re-init notification WebSocket with userId:', u.userId) } catch (_) { void 0 }
          stopWebSocket()
          // 递归以带上 userId 重建
          initWebSocket()
        } else if ((waited += 500) >= 10000) {
          clearInterval(timer)
          try { console.warn('[WS][notify] userId not ready within 10s, keep announcement channel only') } catch (_) { void 0 }
        }
      }, 500)
    } catch (_) { /* 静默失败 */ }
  }
}

// 停止 WebSocket 连接
const stopWebSocket = () => {
  if (notificationWs) {
    try { console.info('[WS][notify] notificationWs.stop()') } catch (_) { void 0 }
    notificationWs.stop()
    notificationWs = null
  }
}

// 初始化通知数据
const initNotifications = async () => {
  try { console.info('[WS][notify] initNotifications -> bootstrap fetch') } catch (_) { void 0 }
  await Promise.all([
    fetchMessages(),
    fetchAnnouncements()
  ])
}

// 重置状态
const resetNotifications = () => {
  try { console.info('[WS][notify] resetNotifications') } catch (_) { void 0 }
  notifications.value = []
  announcements.value = []
  unreadMessageCount.value = 0
  unreadAnnouncementCount.value = 0
  currentAnnouncement.value = null
  showAnnouncementPopup.value = false
  stopWebSocket()
}

export function useNotificationStore() {
  return {
    // 状态
    notifications,
    announcements,
    unreadMessageCount,
    unreadAnnouncementCount,
    currentAnnouncement,
    showAnnouncementPopup,
    totalUnreadCount,
    
    // 方法
    fetchMessages,
    fetchAnnouncements,
    markMessageAsRead,
    markAllMessagesAsRead,
    markAnnouncementAsRead,
    showAnnouncement,
    closeAnnouncementPopup,
    initWebSocket,
    stopWebSocket,
    initNotifications,
    resetNotifications
  }
}
