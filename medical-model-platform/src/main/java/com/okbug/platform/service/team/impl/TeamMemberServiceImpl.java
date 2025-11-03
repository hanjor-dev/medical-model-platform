/**
 * 团队成员服务实现
 *
 * 职责：基础成员关系的便捷查询与插入操作。
 * 约定：仅在需要变更团队成员关系时记录 INFO 日志；查询保持 DEBUG 级别。
 */
package com.okbug.platform.service.team.impl;

import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.service.team.TeamMemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamMemberMapper teamMemberMapper;

    @Override
    /**
     * 判断用户是否为团队启用成员。
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return true 表示存在启用成员关系；参数缺失或不存在则返回 false
     */
    public boolean isMember(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            return false;
        }
        LambdaQueryWrapper<TeamMember> query = new LambdaQueryWrapper<>();
        query.eq(TeamMember::getTeamId, teamId)
             .eq(TeamMember::getUserId, userId)
             .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED);
        return teamMemberMapper.selectCount(query) > 0;
    }

    @Override
    /**
     * 若不存在启用成员关系，则以 MEMBER 角色添加成员。
     *
     * @param teamId 团队ID，不能为空
     * @param userId 用户ID，不能为空
     * @throws ServiceException 当参数缺失时抛出 PARAM_MISSING
     */
    public void addMemberIfAbsent(Long teamId, Long userId) {
        if (teamId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "teamId 不能为空");
        }
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "userId 不能为空");
        }
        if (isMember(teamId, userId)) {
            return;
        }
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setTeamRole(TeamMember.ROLE_MEMBER);
        member.setStatus(TeamMember.STATUS_ENABLED);
        member.setJoinedAt(java.time.LocalDateTime.now());
        teamMemberMapper.insert(member);
        log.info("Member added: teamId={}, userId={}", teamId, userId);
    }

    @Override
    /**
     * 获取用户第一条启用的成员关系。
     *
     * @param userId 用户ID
     * @return 成员关系；当 userId 为空或不存在启用成员关系时返回 null
     */
    public TeamMember getFirstActiveMembership(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
         .last("limit 1");
        return teamMemberMapper.selectOne(q);
    }
}


