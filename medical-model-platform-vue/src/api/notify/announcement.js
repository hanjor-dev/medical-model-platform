import request from '@/utils/request'

export const announcementApi = {
  create(data) {
    return request({ url: '/api/notify/admin/announcement', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/api/notify/admin/announcement/${id}`, method: 'put', data })
  },
  publish(id, body) {
    return request({ url: `/api/notify/admin/announcement/${id}/publish`, method: 'post', data: body })
  },
  offline(id) {
    return request({ url: `/api/notify/admin/announcement/${id}/offline`, method: 'post' })
  },
  getList(params) {
    return request({ url: '/api/notify/admin/announcement/list', method: 'get', params })
  },
  // 用户端：可见公告分页
  getVisible(params) {
    return request({ url: '/api/notify/announcement/visible', method: 'get', params })
  },
  // 用户端：公告详情
  detail(id) {
    return request({ url: `/api/notify/announcement/${id}` , method: 'get' })
  },
  // 用户端：未读且可见公告分页
  getVisibleUnread(params) {
    return request({ url: '/api/notify/announcement/visible/unread', method: 'get', params })
  },
  // 用户端：标记公告已读
  read(id) {
    return request({ url: `/api/notify/announcement/${id}/read`, method: 'post' })
  }
}


