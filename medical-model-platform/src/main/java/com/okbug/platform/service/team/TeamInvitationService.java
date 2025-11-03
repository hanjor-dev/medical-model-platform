/**
 * 团队邀请服务接口
 * 定义团队邀请的创建、撤回与接受等核心业务方法，统一由 Service 层抛出业务异常，
 * Controller 层仅负责鉴权与参数接收；所有返回结果在 Controller 层包装为 ApiResult。
 */
package com.okbug.platform.service.team;

public interface TeamInvitationService {
    /**
     * 创建邀请
     *
     * @param inviterUserId 邀请发起人用户ID
     * @param teamId 团队ID
     * @param invitedUserId 受邀用户ID（可选，与邮箱/手机号三选一）
     * @param invitedEmail 受邀邮箱（可选）
     * @param invitedPhone 受邀手机号（可选）
     * @param role 赋予角色（为空则默认为成员）
     * @return 邀请 token
     */
    String createInvitation(Long inviterUserId, Long teamId, Long invitedUserId, String invitedEmail, String invitedPhone, String role);

    /**
     * 撤回邀请
     *
     * @param invitationId 邀请记录ID
     * @param operatorUserId 操作人用户ID
     */
    void revokeInvitation(Long invitationId, Long operatorUserId);

    /**
     * 接受邀请
     *
     * @param token 邀请 token
     * @param acceptUserId 接受邀请的用户ID
     */
    void acceptInvitation(String token, Long acceptUserId);

    /**
     * 邀请列表分页查询（含权限校验）。默认仅返回未过期、待处理。
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.okbug.platform.entity.team.TeamInvitation> listInvitations(
            Long operatorUserId,
            Long teamId,
            Integer pageNum,
            Integer pageSize,
            Integer status,
            Long invitedUserId
    );

    /**
     * 获取邀请基础链接（从系统配置），仅做简单读取。
     */
    String getInviteBaseUrl();
}


