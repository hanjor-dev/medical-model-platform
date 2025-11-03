/**
 * 团队邀请服务实现：后续补充具体业务逻辑
 */
package com.okbug.platform.service.team.impl;

import com.okbug.platform.mapper.team.TeamInvitationMapper;
import com.okbug.platform.service.team.TeamInvitationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.team.TeamInvitation;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.mapper.team.TeamMapper;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.service.security.TeamAccessService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.common.constants.SystemConfigKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamInvitationServiceImpl implements TeamInvitationService {

    private final TeamInvitationMapper teamInvitationMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;
    private final TeamAccessService teamAccessService;
    private final SystemConfigService systemConfigService;

    /**
     * 创建邀请记录，默认有效期7天。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createInvitation(Long inviterUserId, Long teamId, Long invitedUserId, String invitedEmail, String invitedPhone, String role) {
        // 权限与团队状态校验
        if (!teamAccessService.canManageTeam(inviterUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权创建邀请");
        }
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() == null || team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        TeamInvitation inv = new TeamInvitation();
        inv.setTeamId(teamId);
        inv.setInvitedUserId(invitedUserId);
        inv.setInvitedEmail(invitedEmail);
        inv.setInvitedPhone(invitedPhone);
        inv.setInviterUserId(inviterUserId);
        inv.setTeamRole(StringUtils.hasText(role) ? role : TeamMember.ROLE_MEMBER);
        inv.setInvitationToken(token);
        inv.setExpireTime(LocalDateTime.now().plusDays(7));
        inv.setStatus(TeamInvitation.PENDING);
        teamInvitationMapper.insert(inv);
        log.info("Invitation created: teamId={}, inviter={}, invitedUserId={}, email={}, phone={}, token={}",
                teamId, inviterUserId, invitedUserId, invitedEmail, invitedPhone, token);
        return token;
    }

    /**
     * 撤回邀请（将状态置为 REJECTED）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeInvitation(Long invitationId, Long operatorUserId) {
        TeamInvitation inv = teamInvitationMapper.selectById(invitationId);
        if (inv == null) {
            throw new ServiceException(ErrorCode.INVITATION_NOT_FOUND);
        }
        if (!teamAccessService.canManageTeam(operatorUserId, inv.getTeamId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权撤回邀请");
        }
        inv.setStatus(TeamInvitation.REJECTED);
        teamInvitationMapper.updateById(inv);
        log.info("Invitation revoked: id={}, teamId={}, operator={}", invitationId, inv.getTeamId(), operatorUserId);
    }

    /**
     * 接受邀请：校验 token、状态与有效期，随后入组或激活成员。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptInvitation(String token, Long acceptUserId) {
        LambdaQueryWrapper<TeamInvitation> q = new LambdaQueryWrapper<>();
        q.eq(TeamInvitation::getInvitationToken, token).last("limit 1");
        TeamInvitation inv = teamInvitationMapper.selectOne(q);
        if (inv == null) {
            throw new ServiceException(ErrorCode.INVITATION_TOKEN_INVALID);
        }
        if (inv.getStatus() != TeamInvitation.PENDING) {
            throw new ServiceException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
        }
        // 团队状态校验
        Team team = teamMapper.selectById(inv.getTeamId());
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() == null || team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        if (inv.getExpireTime() != null && inv.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ErrorCode.INVITATION_EXPIRED);
        }
        LambdaQueryWrapper<TeamMember> memQ = new LambdaQueryWrapper<>();
        memQ.eq(TeamMember::getTeamId, inv.getTeamId())
            .eq(TeamMember::getUserId, acceptUserId)
            .last("limit 1");
        TeamMember existing = teamMemberMapper.selectOne(memQ);
        if (existing == null) {
            TeamMember m = new TeamMember();
            m.setTeamId(inv.getTeamId());
            m.setUserId(acceptUserId);
            m.setTeamRole(StringUtils.hasText(inv.getTeamRole()) ? inv.getTeamRole() : TeamMember.ROLE_MEMBER);
            m.setStatus(TeamMember.STATUS_ENABLED);
            m.setJoinedAt(LocalDateTime.now());
            teamMemberMapper.insert(m);
        } else {
            existing.setStatus(TeamMember.STATUS_ENABLED);
            teamMemberMapper.updateById(existing);
        }
        inv.setStatus(TeamInvitation.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        teamInvitationMapper.updateById(inv);
        log.info("Invitation accepted: token={}, teamId={}, userId={}", token, inv.getTeamId(), acceptUserId);
    }

    /**
     * 邀请分页列表（权限校验 + 默认条件）。
     */
    @Override
    public IPage<TeamInvitation> listInvitations(Long operatorUserId, Long teamId, Integer pageNum, Integer pageSize, Integer status, Long invitedUserId) {
        if (!teamAccessService.canViewTeamForManagement(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权查看邀请");
        }
        LambdaQueryWrapper<TeamInvitation> q = new LambdaQueryWrapper<>();
        q.eq(TeamInvitation::getTeamId, teamId)
         .gt(TeamInvitation::getExpireTime, LocalDateTime.now());
        if (status != null) {
            q.eq(TeamInvitation::getStatus, status);
        } else {
            q.eq(TeamInvitation::getStatus, TeamInvitation.PENDING);
        }
        if (invitedUserId != null) {
            q.eq(TeamInvitation::getInvitedUserId, invitedUserId);
        }
        Page<TeamInvitation> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<TeamInvitation> result = teamInvitationMapper.selectPage(page, q);
        log.debug("invitation page queried: teamId={}, operator={}, total={}", teamId, operatorUserId, result.getTotal());
        return result;
    }

    /**
     * 读取系统配置中的邀请基础链接。
     */
    @Override
    public String getInviteBaseUrl() {
        return systemConfigService.getConfigValue(SystemConfigKeys.TEAM_INVITE_BASE_URL,
                SystemConfigKeys.getDefaultValue(SystemConfigKeys.TEAM_INVITE_BASE_URL));
    }
}


