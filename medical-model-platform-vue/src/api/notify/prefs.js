import request from '@/utils/request'

export const notifyPrefsApi = {
  // 获取我的通知渠道偏好列表
  getMyChannelPrefs() {
    return request({ url: '/api/notify/user/prefs/channels', method: 'get' })
  },
  // 获取渠道选项（DICT_4.1 子项）
  getChannelOptions() {
    return request({ url: '/api/notify/user/prefs/options/channels', method: 'get' })
  },
  // 获取类型选项（DICT_4.2 子项）
  getTypeOptions() {
    return request({ url: '/api/notify/user/prefs/options/types', method: 'get' })
  },
  // 保存我的通知渠道偏好列表
  saveMyChannelPrefs(prefs) {
    // prefs: Array<{ channelCode: string, enabled: number }>
    return request({ url: '/api/notify/user/prefs/channels', method: 'put', data: prefs || [] })
  },
  // 获取我的通知类型偏好列表
  getMyTypePrefs() {
    return request({ url: '/api/notify/user/prefs/types', method: 'get' })
  },
  // 保存我的通知类型偏好列表
  saveMyTypePrefs(prefs) {
    // prefs: Array<{ typeCode: string, enabled: number }>
    return request({ url: '/api/notify/user/prefs/types', method: 'put', data: prefs || [] })
  }
}

export default notifyPrefsApi

