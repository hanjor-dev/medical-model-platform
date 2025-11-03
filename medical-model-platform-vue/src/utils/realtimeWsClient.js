import { wsConfig } from './wsConfig'

const defaultBackoff = { initial: 1000, max: 15000, factor: 1.7, jitter: 0.2 }

function withBackoff(nextDelay) {
  const jitter = (Math.random() * 2 - 1) * defaultBackoff.jitter * nextDelay
  let ms = nextDelay + jitter
  if (ms < defaultBackoff.initial) ms = defaultBackoff.initial
  if (ms > defaultBackoff.max) ms = defaultBackoff.max
  return ms
}

export class SingleTopicClient {
  constructor(topicPath, onMessage) {
    this.topicPath = topicPath
    this.onMessage = onMessage
    this.ws = null
    this.stopped = false
    this.delay = defaultBackoff.initial
    this.timer = null
    this.attempt = 0
    try {
      console.info(`[WS] client init -> topic: ${this.topicPath}`)
    }
    // eslint-disable-next-line no-empty
    catch (_) { void 0 }
  }

  connect() {
    if (this.stopped) return
    const url = wsConfig.urlOf(this.topicPath)
    if (!url) return
    try {
      this.attempt += 1
      console.info(`[WS] connecting #${this.attempt} -> ${url}`)
      this.ws = new WebSocket(url)
      this.ws.onopen = () => {
        this.delay = defaultBackoff.initial
        console.info(`[WS] opened -> ${url}`)
      }
      this.ws.onmessage = (evt) => {
        try {
          const data = JSON.parse(evt.data)
          console.debug(`[WS] message <- topic: ${this.topicPath}`, data)
          this.onMessage && this.onMessage(data)
        }
        catch (e) {
          try {
            console.error(`[WS] message parse error <- topic: ${this.topicPath}`, e, {
              raw: String(evt && evt.data).slice(0, 200)
            })
          }
          // eslint-disable-next-line no-empty
          catch (_) { void 0 }
        }
      }
      this.ws.onclose = (evt) => {
        this.ws = null
        const code = evt && evt.code
        const reason = evt && evt.reason
        console.warn(`[WS] closed -> topic: ${this.topicPath}, code: ${code}, reason: ${reason}`)
        if (!this.stopped) {
          const next = withBackoff(this.delay)
          this.delay = next
          clearTimeout(this.timer)
          console.info(`[WS] reconnect in ${Math.round(next)}ms -> ${url}`)
          this.timer = setTimeout(() => this.connect(), next)
        }
      }
      this.ws.onerror = (err) => {
        console.error(`[WS] error -> topic: ${this.topicPath}`, err)
        try {
          this.ws && this.ws.close()
        }
        // eslint-disable-next-line no-empty
        catch (_) { /* noop */ void 0 }
      }
    } catch (_) {
      console.error('[WS] construct WebSocket failed, will retry')
      const next = withBackoff(this.delay)
      this.delay = next
      clearTimeout(this.timer)
      this.timer = setTimeout(() => this.connect(), next)
    }
  }

  start() {
    try { console.info(`[WS] start -> topic: ${this.topicPath}`) } catch (_) { void 0 }
    this.stopped = false
    this.connect()
  }
  stop() {
    this.stopped = true
    clearTimeout(this.timer)
    if (this.ws) {
      try {
        this.ws.close()
      }
      // eslint-disable-next-line no-empty
      catch (_) { /* noop */ void 0 }
      this.ws = null
    }
    try { console.info(`[WS] stop -> topic: ${this.topicPath}`) } catch (_) { void 0 }
  }
}

export class RealtimeWsClient {
  constructor({ onRoleChanged, onUserChanged, onNewMessage, onNewAnnouncement, userId } = {}) {
    this.roleClient = new SingleTopicClient(wsConfig.topics.roleChanged, onRoleChanged)
    this.userClient = new SingleTopicClient(wsConfig.topics.userChanged, onUserChanged)
    const messagePath = appendQuery(wsConfig.topics.userMessage, userId ? { userId } : null)
    this.messageClient = userId ? new SingleTopicClient(messagePath, onNewMessage) : null
    this.announcementClient = new SingleTopicClient(wsConfig.topics.userAnnouncement, onNewAnnouncement)
    try {
      console.info('[WS] RealtimeWsClient init -> topics:', {
        roleChanged: wsConfig.topics.roleChanged,
        userChanged: wsConfig.topics.userChanged,
        userMessage: this.messageClient ? messagePath : '(disabled: no userId)',
        userAnnouncement: wsConfig.topics.userAnnouncement
      })
    }
    // eslint-disable-next-line no-empty
    catch (_) { void 0 }
  }

  start() {
    try { console.info('[WS] RealtimeWsClient start') } catch (_) { void 0 }
    this.roleClient.start()
    this.userClient.start()
    if (this.messageClient) this.messageClient.start()
    this.announcementClient.start()
  }

  stop() {
    try { console.info('[WS] RealtimeWsClient stop') } catch (_) { void 0 }
    this.roleClient.stop()
    this.userClient.stop()
    if (this.messageClient) this.messageClient.stop()
    this.announcementClient.stop()
  }
}

// Back-compat factory exports
export function createRealtimeWsClient(handlers) { return new RealtimeWsClient(handlers) }
export function createPermissionWsClient({ onRoleChanged, onUserChanged } = {}) {
  const roleClient = new SingleTopicClient(wsConfig.topics.roleChanged, onRoleChanged)
  const userClient = new SingleTopicClient(wsConfig.topics.userChanged, onUserChanged)
  try {
    console.info('[WS] PermissionWs init -> topics:', {
      roleChanged: wsConfig.topics.roleChanged,
      userChanged: wsConfig.topics.userChanged
    })
  }
  // eslint-disable-next-line no-empty
  catch (_) { void 0 }
  return {
    start() {
      try { console.info('[WS] PermissionWs start') } catch (_) { void 0 }
      roleClient.start()
      userClient.start()
    },
    stop() {
      try { console.info('[WS] PermissionWs stop') } catch (_) { void 0 }
      roleClient.stop()
      userClient.stop()
    }
  }
}
function appendQuery(path, queryObj) {
  if (!queryObj || typeof queryObj !== 'object') return path
  const parts = []
  for (const k in queryObj) {
    if (queryObj[k] === undefined || queryObj[k] === null || queryObj[k] === '') continue
    parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(queryObj[k])}`)
  }
  if (parts.length === 0) return path
  return `${path}${path.includes('?') ? '&' : '?'}${parts.join('&')}`
}

export function createNotificationWsClient({ onNewMessage, onNewAnnouncement, userId } = {}) {
  const messagePath = appendQuery(wsConfig.topics.userMessage, userId ? { userId } : null)
  // 公告为全量 topic 广播，不携带 userId
  const announcePath = wsConfig.topics.userAnnouncement
  // 只有在拿到 userId 时才创建用户消息通道，避免后端因缺少 userId 拒绝连接
  const message = userId ? new SingleTopicClient(messagePath, onNewMessage) : null
  const announce = new SingleTopicClient(announcePath, onNewAnnouncement)
  try {
    console.info('[WS] createNotificationWsClient ->', { userId, messagePath, announcePath })
  }
  // eslint-disable-next-line no-empty
  catch (_) { void 0 }
  return {
    start() {
      try { console.info('[WS] notification.start()') } catch (_) { void 0 }
      if (message) message.start(); announce.start()
    },
    stop() {
      try { console.info('[WS] notification.stop()') } catch (_) { void 0 }
      if (message) message.stop(); announce.stop()
    }
  }
}

export default RealtimeWsClient


