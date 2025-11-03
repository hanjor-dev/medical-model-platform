import request from '@/utils/request'

export const teamJoinRequestApi = {
  // 提交加入申请
  submit(data) {
    return request({ url: '/api/team/join-request', method: 'post', data })
  },
  // 审批：前端action(APPROVE/REJECT) 映射为后端approve(boolean)
  process(data) {
    const payload = {
      requestId: data.requestId,
      approve: String(data.action).toUpperCase() === 'APPROVE',
      reason: data.reason
    }
    return request({ url: '/api/team/join-request/process', method: 'post', data: payload })
  },
  // 撤销（预留）
  cancel(requestId) {
    return request({ url: `/api/team/join-request/${requestId}`, method: 'delete' })
  },
  // 列表：前端状态/申请人等参数做向后端的轻映射
  list(teamId, params) {
    const mapped = { pageNum: params?.pageNum, pageSize: params?.pageSize }
    // 状态映射：字符串 -> 数字
    if (params?.status) {
      const s = String(params.status).toUpperCase()
      const map = { PENDING: 0, APPROVED: 1, REJECTED: 2 }
      if (map[s] !== undefined) mapped.status = map[s]
    }
    // 申请人关键词：直接传 applicantKeyword（支持昵称/用户名模糊）
    if (params?.applicant) {
      mapped.applicantKeyword = String(params.applicant).trim()
    }
    // 备注关键词：映射为 requestReasonKeyword
    if (params?.keyword) {
      mapped.requestReasonKeyword = String(params.keyword).trim()
    }
    // 时间范围：后端接收 beginTime / endTime（YYYY-MM-DD HH:mm:ss）
    if (params?.start) mapped.beginTime = params.start
    if (params?.end) mapped.endTime = params.end
    return request({ url: `/api/team/join-request/${teamId}`, method: 'get', params: mapped })
  }
  ,
  // 详情
  getJoinRequestDetail(requestId) {
    return request({ url: `/api/team/join-request/detail/${requestId}`, method: 'get' })
  }
}

export default teamJoinRequestApi

