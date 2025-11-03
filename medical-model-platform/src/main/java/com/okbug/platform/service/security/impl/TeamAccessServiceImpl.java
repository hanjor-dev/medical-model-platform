package com.okbug.platform.service.security.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.mapper.team.TeamMapper;
import com.okbug.platform.service.security.TeamAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 团队访问边界校验实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamAccessServiceImpl implements TeamAccessService {

    private final UserMapper userMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;

    /**
     * 判断操作者是否可以管理目标用户。
     * 规则：同人或 SUPER_ADMIN 或同一有效团队且操作者为 OWNER/ADMIN，且团队处于启用状态。
     *
     * @param operatorUserId 操作人用户ID
     * @param targetUserId 目标用户ID
     * @return 是否可管理
     */
    @Override
    public boolean canManageUser(Long operatorUserId, Long targetUserId) {
        log.debug("canManageUser: operatorUserId={}, targetUserId={}", operatorUserId, targetUserId);
        if (operatorUserId == null || targetUserId == null) {
            return false;
        }
        if (Objects.equals(operatorUserId, targetUserId)) {
            log.debug("canManageUser: same user, allow");
            return true;
        }
        User operator = userMapper.selectById(operatorUserId);
        if (operator != null && operator.isSuperAdmin()) {
            log.debug("canManageUser: operator is SUPER_ADMIN, allow");
            return true;
        }
        // 同团队且操作者在团队内角色为 OWNER/ADMIN
        Long operatorTeamId = getActiveTeamId(operatorUserId);
        Long targetTeamId = getActiveTeamId(targetUserId);
        if (operatorTeamId == null || !Objects.equals(operatorTeamId, targetTeamId)) {
            log.debug("canManageUser: not same active team or no team, deny");
            return false;
        }
        // 团队被禁用时，非超级管理员无权管理
        if (!isTeamEnabled(operatorTeamId)) {
            log.warn("canManageUser: team disabled, teamId={}, deny", operatorTeamId);
            return false;
        }
        String operatorRole = getTeamRole(operatorTeamId, operatorUserId);
        boolean allow = TeamMember.ROLE_OWNER.equals(operatorRole) || TeamMember.ROLE_ADMIN.equals(operatorRole);
        log.debug("canManageUser: operatorRole={} -> {}", operatorRole, allow);
        return allow;
    }

    /**
     * 判断操作者是否可以管理指定团队。
     * 规则：SUPER_ADMIN 或该团队的 OWNER/ADMIN，且团队处于启用状态。
     *
     * @param operatorUserId 操作人用户ID
     * @param teamId 团队ID
     * @return 是否可管理
     */
    @Override
    public boolean canManageTeam(Long operatorUserId, Long teamId) {
        log.debug("canManageTeam: operatorUserId={}, teamId={}", operatorUserId, teamId);
        if (operatorUserId == null || teamId == null) {
            return false;
        }
        User operator = userMapper.selectById(operatorUserId);
        if (operator != null && operator.isSuperAdmin()) {
            log.debug("canManageTeam: operator is SUPER_ADMIN, allow");
            return true;
        }
        // 团队被禁用时，非超级管理员不可管理
        if (!isTeamEnabled(teamId)) {
            log.warn("canManageTeam: team disabled, teamId={}, deny", teamId);
            return false;
        }
        String operatorRole = getTeamRole(teamId, operatorUserId);
        boolean allow = TeamMember.ROLE_OWNER.equals(operatorRole) || TeamMember.ROLE_ADMIN.equals(operatorRole);
        log.debug("canManageTeam: operatorRole={} -> {}", operatorRole, allow);
        return allow;
    }

    /**
     * 判断操作者是否可以查看团队管理数据。
     * 规则：SUPER_ADMIN 或该团队的 OWNER/ADMIN；不受团队禁用影响（仅查看）。
     *
     * @param operatorUserId 操作人用户ID
     * @param teamId 团队ID
     * @return 是否可查看管理数据
     */
    @Override
    public boolean canViewTeamForManagement(Long operatorUserId, Long teamId) {
        log.debug("canViewTeamForManagement: operatorUserId={}, teamId={}", operatorUserId, teamId);
        if (operatorUserId == null || teamId == null) {
            return false;
        }
        User operator = userMapper.selectById(operatorUserId);
        if (operator != null && operator.isSuperAdmin()) {
            log.debug("canViewTeamForManagement: operator is SUPER_ADMIN, allow");
            return true;
        }
        // 查看型权限不受团队禁用影响，只要是 OWNER/ADMIN 即可
        String operatorRole = getTeamRole(teamId, operatorUserId);
        boolean allow = TeamMember.ROLE_OWNER.equals(operatorRole) || TeamMember.ROLE_ADMIN.equals(operatorRole);
        log.debug("canViewTeamForManagement: operatorRole={} -> {}", operatorRole, allow);
        return allow;
    }

    /**
     * 判断两个用户是否属于同一个有效团队（成员关系启用）。
     *
     * @param userIdA 用户A
     * @param userIdB 用户B
     * @return 是否同团队
     */
    @Override
    public boolean isSameTeam(Long userIdA, Long userIdB) {
        if (userIdA == null || userIdB == null) {
            return false;
        }
        Long teamA = getActiveTeamId(userIdA);
        Long teamB = getActiveTeamId(userIdB);
        boolean same = teamA != null && Objects.equals(teamA, teamB);
        log.debug("isSameTeam: userIdA={}, userIdB={}, teamA={}, teamB={}, same={}", userIdA, userIdB, teamA, teamB, same);
        return same;
    }

    /**
     * 返回操作者可管理的用户ID集合（包含自己）。
     * 规则：SUPER_ADMIN 返回全量用户ID；否则返回同团队启用成员ID集合，若不在团队仅返回自己。
     *
     * @param operatorUserId 操作人用户ID
     * @return 可管理的用户ID集合
     */
    @Override
    public List<Long> listManagedUserIds(Long operatorUserId) {
        log.debug("listManagedUserIds: operatorUserId={}", operatorUserId);
        if (operatorUserId == null) {
            return Collections.emptyList();
        }
        User operator = userMapper.selectById(operatorUserId);
        if (operator != null && operator.isSuperAdmin()) {
            // 超级管理员返回全量用户ID（谨慎：用于管理范围查询时建议改为不限制）
            List<Long> allIds = userMapper.selectList(null).stream().map(User::getId).collect(Collectors.toList());
            log.debug("listManagedUserIds: SUPER_ADMIN, size={}", allIds.size());
            return allIds;
        }
        Long teamId = getActiveTeamId(operatorUserId);
        if (teamId == null) {
            log.debug("listManagedUserIds: no active team, return self");
            return Collections.singletonList(operatorUserId);
        }
        // 团队内全部用户ID
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED);
        List<TeamMember> members = teamMemberMapper.selectList(q);
        if (members == null || members.isEmpty()) {
            log.debug("listManagedUserIds: team has no enabled members, return self");
            return Collections.singletonList(operatorUserId);
        }
        List<Long> ids = members.stream().map(TeamMember::getUserId).distinct().collect(Collectors.toList());
        log.debug("listManagedUserIds: teamId={}, size={}", teamId, ids.size());
        return ids;
    }

    private Long getActiveTeamId(Long userId) {
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
         .last("limit 1");
        TeamMember m = teamMemberMapper.selectOne(q);
        return m == null ? null : m.getTeamId();
    }

    private String getTeamRole(Long teamId, Long userId) {
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
         .last("limit 1");
        TeamMember m = teamMemberMapper.selectOne(q);
        return m == null ? null : m.getTeamRole();
    }

    private boolean isTeamEnabled(Long teamId) {
        if (teamId == null) {
            return false;
        }
        Team t = teamMapper.selectById(teamId);
        if (t == null) {
            return false;
        }
        Integer status = t.getStatus();
        return status != null && status == Team.STATUS_ENABLED;
    }
}


