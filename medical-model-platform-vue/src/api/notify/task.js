import request from '@/utils/request'

export const taskApi = {
  getLogs(taskId, params) {
    return request({ url: `/api/v1/notify/admin/task/${taskId}/logs`, method: 'get', params })
  }
}


