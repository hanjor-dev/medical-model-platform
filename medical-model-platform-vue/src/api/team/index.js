import request from '@/utils/request'

export const teamApi = {
  // 团队列表（普通用户仅返回所属团队）
  list(params) {
    return request({ url: '/api/teams', method: 'get', params })
  },
  // 团队详情
  detail(id) {
    return request({ url: `/api/teams/${id}`, method: 'get' })
  },
  // 创建团队
  create(data) {
    return request({ url: '/api/teams', method: 'post', data })
  },
  // 更新团队
  update(id, data) {
    return request({ url: `/api/teams/${id}`, method: 'put', data })
  },
  // 设置团队状态（仅 SUPER_ADMIN）
  setStatus(id, status) {
    return request({ url: `/api/teams/${id}/status`, method: 'put', data: { status } })
  },
  // 通过团队码预览团队
  previewByCode(teamCode) {
    return request({ url: '/api/teams/preview-by-code', method: 'get', params: { teamCode } })
  },
  // 删除团队（软删）
  remove(id) {
    return request({ url: `/api/teams/${id}`, method: 'delete' })
  },
  // 成员列表
  listMembers(teamId, params) {
    return request({ url: `/api/teams/${teamId}/members`, method: 'get', params })
  },
  // 添加成员
  addMember(teamId, data) {
    return request({ url: `/api/teams/${teamId}/members`, method: 'post', data })
  },
  // 更新成员
  updateMember(teamId, userId, data) {
    return request({ url: `/api/teams/${teamId}/members/${userId}`, method: 'put', data })
  },
  // 移除成员
  removeMember(teamId, userId) {
    return request({ url: `/api/teams/${teamId}/members/${userId}`, method: 'delete' })
  },
  // 转移团队拥有者
  transferOwner(teamId, data) {
    return request({ url: `/api/teams/${teamId}/transfer-owner`, method: 'post', data })
  },
  // 退出团队（当前登录用户）
  exit(teamId) {
    return request({ url: `/api/teams/${teamId}/exit`, method: 'post' })
  },
  // 解散团队（仅拥有者）
  dissolve(teamId) {
    return request({ url: `/api/teams/${teamId}/dissolve`, method: 'post' })
  }
}

export default teamApi


