import request from '@/utils/request'

export const messageApi = {
  // 管理端：创建站内消息（直接内容） data: { userId, content, messageType?, title?, scheduleTime? }
  create(data) {
    return request({ url: '/api/notify/admin/message', method: 'post', data })
  },
  // 管理端：取消消息
  cancel(id) {
    return request({ url: `/api/notify/admin/message/${id}/cancel`, method: 'post' })
  },
  // 管理端：分页列表
  getList(params) {
    return request({ url: '/api/notify/admin/message/list', method: 'get', params })
  },
  // 用户端：我的消息列表
  getMyList(params) {
    return request({ url: '/api/notify/user/message/list', method: 'get', params })
  },
  // 用户端：消息详情
  getMyDetail(id) {
    return request({ url: `/api/notify/user/message/${id}` , method: 'get' })
  },
  // 用户端：标记消息已读
  markRead(id) {
    return request({ url: `/api/notify/user/message/${id}/read`, method: 'post' })
  },
  // 用户端：将全部消息设为已读
  markAllRead() {
    return request({ url: '/api/notify/user/message/read-all', method: 'post' })
  }
}


