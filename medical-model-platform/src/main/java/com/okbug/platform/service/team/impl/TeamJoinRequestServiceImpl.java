/**
 * 团队加入申请服务实现：后续补充具体业务逻辑
 */
package com.okbug.platform.service.team.impl;

import com.okbug.platform.mapper.team.TeamJoinRequestMapper;
import com.okbug.platform.service.team.TeamJoinRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.team.TeamJoinRequest;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.mapper.team.TeamMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.limiter.RateLimitService;
import com.okbug.platform.common.limiter.IdempotencyService;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.ws.RealtimeWebSocketHandler;
import com.okbug.platform.dto.team.JoinRequestSubmit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;

import com.okbug.platform.service.system.message.NotificationFacade;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamJoinRequestServiceImpl implements TeamJoinRequestService {

    private final TeamJoinRequestMapper teamJoinRequestMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;
    private final RateLimitService rateLimitService;
    private final IdempotencyService idempotencyService;
    private final SystemConfigService systemConfigService;
    private final TeamAccessService teamAccessService;
    private final RealtimeWebSocketHandler websocketHandler;
    private final UserMapper userMapper;
    
    private final NotificationFacade notificationFacade;

    /**
     * 查询是否存在用户对某团队的待处理加入申请
     * 仅用于内部快速判断，不抛出业务异常。
     */
    @Override
    public boolean existsPending(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            return false;
        }
        LambdaQueryWrapper<TeamJoinRequest> query = new LambdaQueryWrapper<>();
        query.eq(TeamJoinRequest::getTeamId, teamId)
             .eq(TeamJoinRequest::getUserId, userId)
             .eq(TeamJoinRequest::getStatus, TeamJoinRequest.PENDING)
             .eq(TeamJoinRequest::getIsDeleted, 0);
        return teamJoinRequestMapper.selectCount(query) > 0;
    }

    /**
     * 直接创建一条待处理的加入申请记录。
     * 外部应优先调用 submitJoinRequest() 做完整校验与通知。
     */
    @Override
    public Long createPending(Long teamId, Long userId, String teamCode, String requestReason) {
        TeamJoinRequest r = new TeamJoinRequest();
        r.setTeamId(teamId);
        r.setUserId(userId);
        r.setTeamCode(teamCode);
        r.setRequestReason(requestReason);
        r.setStatus(TeamJoinRequest.PENDING);
        teamJoinRequestMapper.insert(r);
        log.info("JoinRequest created: teamId={}, userId={}, requestId={}", teamId, userId, r.getId());
        return r.getId();
    }

    /**
     * 审批加入申请。通过则将用户加入团队；拒绝仅标记状态。
     * 包含权限校验、重复处理校验与实时通知。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processJoinRequest(Long requestId, Long operatorUserId, boolean approve, String reason) {
        TeamJoinRequest r = teamJoinRequestMapper.selectById(requestId);
        if (r == null) {
            throw new ServiceException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }
        if (!teamAccessService.canManageTeam(operatorUserId, r.getTeamId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权审批该加入申请");
        }
        log.info("JoinRequest processing: requestId={}, teamId={}, operator={}, approve={}, reason={}", requestId, r.getTeamId(), operatorUserId, approve, reason);

        // 禁用团队不允许通过（可允许拒绝）
        if (approve) {
            Team team = teamMapper.selectById(r.getTeamId());
            if (team == null) {
                throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
            }
            if (team.getStatus() == null || team.getStatus() != Team.STATUS_ENABLED) {
                throw new ServiceException(ErrorCode.TEAM_DISABLED);
            }
        }

        // 条件更新：仅当当前为待处理时才能被处理；不做逻辑删除，保留历史
        LambdaUpdateWrapper<TeamJoinRequest> upd = new LambdaUpdateWrapper<>();
        upd.eq(TeamJoinRequest::getId, requestId)
           .eq(TeamJoinRequest::getStatus, TeamJoinRequest.PENDING)
           .set(TeamJoinRequest::getProcessedBy, operatorUserId)
           .set(TeamJoinRequest::getProcessedAt, LocalDateTime.now())
           .set(TeamJoinRequest::getProcessReason, reason)
           .set(TeamJoinRequest::getStatus, approve ? TeamJoinRequest.APPROVED : TeamJoinRequest.REJECTED);
        int updated = teamJoinRequestMapper.update(null, upd);
        if (updated == 0) {
            throw new ServiceException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }

        if (approve) {
            LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
            q.eq(TeamMember::getTeamId, r.getTeamId())
             .eq(TeamMember::getUserId, r.getUserId())
             .last("limit 1");
            TeamMember existing = teamMemberMapper.selectOne(q);
            if (existing == null) {
                // 先尝试恢复逻辑删除（软删除）的记录；若无则插入
                LambdaUpdateWrapper<TeamMember> revive = new LambdaUpdateWrapper<>();
                revive.eq(TeamMember::getTeamId, r.getTeamId())
                      .eq(TeamMember::getUserId, r.getUserId())
                      .eq(TeamMember::getIsDeleted, 1)
                      .set(TeamMember::getIsDeleted, 0)
                      .set(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                      .set(TeamMember::getTeamRole, TeamMember.ROLE_MEMBER)
                      .set(TeamMember::getJoinedAt, LocalDateTime.now());
                int revived = teamMemberMapper.update(null, revive);
                if (revived == 0) {
                    TeamMember m = new TeamMember();
                    m.setTeamId(r.getTeamId());
                    m.setUserId(r.getUserId());
                    m.setTeamRole(TeamMember.ROLE_MEMBER);
                    m.setStatus(TeamMember.STATUS_ENABLED);
                    m.setJoinedAt(LocalDateTime.now());
                    teamMemberMapper.insert(m);
                }
            } else {
                // 已存在记录：恢复为启用并确保未被逻辑删除
                LambdaUpdateWrapper<TeamMember> memberUpd = new LambdaUpdateWrapper<>();
                memberUpd.eq(TeamMember::getTeamId, r.getTeamId())
                         .eq(TeamMember::getUserId, r.getUserId())
                         .set(TeamMember::getIsDeleted, 0)
                         .set(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                         .set(TeamMember::getJoinedAt, LocalDateTime.now());
                teamMemberMapper.update(null, memberUpd);
            }
        } else {
            // 拒绝已归档，无需额外更新
        }
        websocketHandler.sendTeamJoinRequestChanged(r.getTeamId(), r.getId(), approve ? "approved" : "rejected", operatorUserId);
        if (approve) {
            websocketHandler.sendTeamMemberChanged(r.getTeamId(), r.getUserId(), "add", TeamMember.ROLE_MEMBER);
        }
        log.info("JoinRequest processed: requestId={}, status={}, operator={}", requestId, r.getStatus(), operatorUserId);

        // 推送：审批结果通知申请人（不是管理员）
        try {
            Team t = teamMapper.selectById(r.getTeamId());
            String teamName = t != null ? t.getTeamName() : ("ID=" + r.getTeamId());
            notificationFacade.notifyJoinRequestProcessed(r.getUserId(), teamName, approve, reason, operatorUserId);
        } catch (Exception e) { log.warn("processJoinRequest message push failed: {}", e.getMessage()); }
    }

    /**
     * 提交加入申请。内置限流、幂等、团队/成员校验与通知。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitJoinRequest(Long userId, JoinRequestSubmit request) {
        // 限流
        int limit = systemConfigService.getConfigValueAsInt(SystemConfigKeys.TEAM_JOIN_RATE_LIMIT_PER_MINUTE, 6);
        if (!rateLimitService.allow("rl:team:join:" + userId, limit, java.time.Duration.ofMinutes(1))) {
            throw new ServiceException(ErrorCode.TEAM_RATE_LIMITED);
        }
        // 幂等
        long idemTtl = systemConfigService.getConfigValueAsInt(SystemConfigKeys.TEAM_JOIN_IDEMPOTENCY_TTL_SECONDS, 30).longValue();
        String idemKey = "idem:team:join:" + request.getTeamId() + ":u:" + userId;
        if (!idempotencyService.tryAcquire(idemKey, idemTtl)) {
            throw new ServiceException(ErrorCode.TEAM_IDEMPOTENT_REPLAY);
        }
        Team team = teamMapper.selectById(request.getTeamId());
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() == null || team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        // 已是成员
        LambdaQueryWrapper<TeamMember> memQ = new LambdaQueryWrapper<>();
        memQ.eq(TeamMember::getTeamId, request.getTeamId())
            .eq(TeamMember::getUserId, userId)
            .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED);
        if (teamMemberMapper.selectCount(memQ) > 0) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
        if (existsPending(request.getTeamId(), userId)) {
            throw new ServiceException(ErrorCode.TEAM_JOIN_REQUEST_ALREADY_EXISTS);
        }

        Long requestId;
        try {
            requestId = createPending(request.getTeamId(), userId, request.getTeamCode(), request.getRequestReason());
        } catch (DuplicateKeyException e) {
            // 并发兜底：仅允许一条Pending
            throw new ServiceException(ErrorCode.TEAM_JOIN_REQUEST_ALREADY_EXISTS);
        }
        websocketHandler.sendTeamJoinRequestChanged(request.getTeamId(), requestId, "submitted", userId);
        log.info("JoinRequest submitted: teamId={}, userId={}, requestId={}", request.getTeamId(), userId, requestId);

        // 推送：通知团队拥有者和管理员有新的加入申请
        try {
            java.util.List<Long> targets = listAdminUserIds(request.getTeamId(), true);
            Team t = teamMapper.selectById(request.getTeamId());
            String teamName = t != null ? t.getTeamName() : ("ID=" + request.getTeamId());
            String applicantName = getUserDisplayName(userId);
            for (Long uid : targets) {
                notificationFacade.notifyJoinRequestSubmittedToAdmin(uid, teamName, applicantName, userId);
            }
        } catch (Exception e) { log.warn("submitJoinRequest message push failed: {}", e.getMessage()); }
    }

    /**
     * 申请人撤销自己的加入申请。仅允许撤销待处理状态的申请。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelJoinRequest(Long requestId, Long userId) {
        TeamJoinRequest r = teamJoinRequestMapper.selectById(requestId);
        if (r == null) {
            throw new ServiceException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }
        if (!userId.equals(r.getUserId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "仅申请人可撤销");
        }
        // 条件更新：仅当待处理时允许撤销；不做逻辑删除，保留历史
        LambdaUpdateWrapper<TeamJoinRequest> upd = new LambdaUpdateWrapper<>();
        upd.eq(TeamJoinRequest::getId, requestId)
           .eq(TeamJoinRequest::getUserId, userId)
           .eq(TeamJoinRequest::getStatus, TeamJoinRequest.PENDING)
           .set(TeamJoinRequest::getStatus, TeamJoinRequest.REJECTED)
           .set(TeamJoinRequest::getProcessedBy, userId)
           .set(TeamJoinRequest::getProcessedAt, LocalDateTime.now())
           .set(TeamJoinRequest::getProcessReason, "用户自行撤销");
        int updated = teamJoinRequestMapper.update(null, upd);
        if (updated == 0) {
            throw new ServiceException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }
        websocketHandler.sendTeamJoinRequestChanged(r.getTeamId(), r.getId(), "cancelled", userId);
        log.info("JoinRequest cancelled by applicant: requestId={}, userId={}", requestId, userId);

        // 推送：通知管理员有申请被撤销（可选）
        try {
            java.util.List<Long> targets = listAdminUserIds(r.getTeamId(), true);
            Team t = teamMapper.selectById(r.getTeamId());
            String teamName = t != null ? t.getTeamName() : ("ID=" + r.getTeamId());
            String applicantName = getUserDisplayName(userId);
            for (Long uid : targets) {
                notificationFacade.notifyJoinRequestCancelledToAdmin(uid, teamName, applicantName, userId);
            }
        } catch (Exception e) { log.warn("cancelJoinRequest message push failed: {}", e.getMessage()); }
    }

    /**
     * 分页查询加入申请列表。默认仅返回待处理，支持按申请人过滤。
     */
    @Override
    public IPage<com.okbug.platform.vo.team.TeamJoinRequestVO> listJoinRequests(Long operatorUserId, Long teamId, Integer pageNum, Integer pageSize, Integer status, Long applicantUserId, String applicantKeyword, java.time.LocalDateTime beginTime, java.time.LocalDateTime endTime, String requestReasonKeyword) {
        if (!teamAccessService.canViewTeamForManagement(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权查看加入申请");
        }
        if (status != null && (status < 0 || status > 2)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "status 取值必须为 0/1/2");
        }

        LambdaQueryWrapper<TeamJoinRequest> q = new LambdaQueryWrapper<>();
        q.eq(TeamJoinRequest::getTeamId, teamId);
        if (status != null) {
            q.eq(TeamJoinRequest::getStatus, status);
        }
        if (applicantUserId != null) {
            q.eq(TeamJoinRequest::getUserId, applicantUserId);
        }
        if (beginTime != null) {
            q.ge(TeamJoinRequest::getCreateTime, beginTime);
        }
        if (endTime != null) {
            q.le(TeamJoinRequest::getCreateTime, endTime);
        }
        if (requestReasonKeyword != null && !requestReasonKeyword.trim().isEmpty()) {
            q.like(TeamJoinRequest::getRequestReason, requestReasonKeyword.trim());
        }
        // 申请人关键词：在DB侧先筛选匹配用户ID，保障分页total与数据准确
        if (applicantKeyword != null && !applicantKeyword.trim().isEmpty()) {
            String kw = applicantKeyword.trim();
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.okbug.platform.entity.auth.User> uq = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            uq.select(com.okbug.platform.entity.auth.User::getId)
              .like(com.okbug.platform.entity.auth.User::getNickname, kw)
              .or()
              .like(com.okbug.platform.entity.auth.User::getUsername, kw);
            java.util.List<com.okbug.platform.entity.auth.User> matchedUsers = userMapper.selectList(uq);
            java.util.List<Long> matchedIds = new java.util.ArrayList<>();
            for (com.okbug.platform.entity.auth.User u : matchedUsers) {
                if (u != null && u.getId() != null) {
                    matchedIds.add(u.getId());
                }
            }
            if (matchedIds.isEmpty()) {
                // 无匹配用户，直接返回空结果
                q.eq(TeamJoinRequest::getUserId, -1L);
            } else {
                q.in(TeamJoinRequest::getUserId, matchedIds);
            }
        }
        // 默认按创建时间倒序
        q.orderByDesc(TeamJoinRequest::getCreateTime);
        Page<TeamJoinRequest> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<TeamJoinRequest> result = teamJoinRequestMapper.selectPage(page, q);
        log.debug("JoinRequest page queried: teamId={}, operator={}, total={}", teamId, operatorUserId, result.getTotal());

        // 映射为 VO，并补充申请人昵称
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.okbug.platform.vo.team.TeamJoinRequestVO> voPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        java.util.List<TeamJoinRequest> records = result.getRecords();
        java.util.List<com.okbug.platform.vo.team.TeamJoinRequestVO> voRecords = new java.util.ArrayList<>();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (TeamJoinRequest r : records) {
            if (r.getUserId() != null) userIds.add(r.getUserId());
            if (r.getProcessedBy() != null) userIds.add(r.getProcessedBy());
        }
        java.util.Map<Long, com.okbug.platform.entity.auth.User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            java.util.List<com.okbug.platform.entity.auth.User> users = userMapper.selectBatchIds(userIds);
            for (com.okbug.platform.entity.auth.User u : users) {
                userMap.put(u.getId(), u);
            }
        }
        // DB 已进行了申请人关键词过滤，这里不再做内存端过滤，避免影响分页准确性
        for (TeamJoinRequest r : records) {
            com.okbug.platform.vo.team.TeamJoinRequestVO vo = new com.okbug.platform.vo.team.TeamJoinRequestVO();
            vo.setId(r.getId());
            vo.setTeamId(r.getTeamId());
            vo.setUserId(r.getUserId());
            com.okbug.platform.entity.auth.User u = userMap.get(r.getUserId());
            if (u != null) {
                String name = u.getNickname();
                if (name == null || name.trim().isEmpty()) {
                    name = u.getUsername();
                }
                vo.setApplicantName(name);
            }
            // 注意：关键词过滤应尽量在DB完成以保证分页准确；
            // 当前版本暂在内存端兜底过滤（如需严格分页，请改为JOIN用户表LIKE过滤）。
            // 不再进行内存端关键词过滤
            vo.setRequestReason(r.getRequestReason());
            vo.setTeamCode(r.getTeamCode());
            vo.setStatus(r.getStatus());
            vo.setProcessedBy(r.getProcessedBy());
            if (r.getProcessedBy() != null) {
                com.okbug.platform.entity.auth.User p = userMap.get(r.getProcessedBy());
                if (p != null) {
                    String pname = p.getNickname();
                    if (pname == null || pname.trim().isEmpty()) {
                        pname = p.getUsername();
                    }
                    vo.setProcessedByName(pname);
                }
            }
            vo.setProcessedAt(r.getProcessedAt());
            vo.setProcessReason(r.getProcessReason());
            vo.setCreateTime(r.getCreateTime());
            vo.setUpdateTime(r.getUpdateTime());
            voRecords.add(vo);
        }
        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public com.okbug.platform.vo.team.TeamJoinRequestVO getJoinRequestDetail(Long operatorUserId, Long requestId) {
        TeamJoinRequest r = teamJoinRequestMapper.selectById(requestId);
        if (r == null) {
            throw new ServiceException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }
        if (!teamAccessService.canViewTeamForManagement(operatorUserId, r.getTeamId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权查看加入申请详情");
        }
        com.okbug.platform.vo.team.TeamJoinRequestVO vo = new com.okbug.platform.vo.team.TeamJoinRequestVO();
        vo.setId(r.getId());
        vo.setTeamId(r.getTeamId());
        vo.setUserId(r.getUserId());
        com.okbug.platform.entity.auth.User u = r.getUserId() == null ? null : userMapper.selectById(r.getUserId());
        if (u != null) {
            String name = u.getNickname();
            if (name == null || name.trim().isEmpty()) {
                name = u.getUsername();
            }
            vo.setApplicantName(name);
        }
        vo.setRequestReason(r.getRequestReason());
        vo.setTeamCode(r.getTeamCode());
        vo.setStatus(r.getStatus());
        vo.setProcessedBy(r.getProcessedBy());
        vo.setProcessedAt(r.getProcessedAt());
        vo.setProcessReason(r.getProcessReason());
        if (r.getProcessedBy() != null) {
            com.okbug.platform.entity.auth.User p = userMapper.selectById(r.getProcessedBy());
            if (p != null) {
                String pname = p.getNickname();
                if (pname == null || pname.trim().isEmpty()) {
                    pname = p.getUsername();
                }
                vo.setProcessedByName(pname);
            }
        }
        vo.setCreateTime(r.getCreateTime());
        vo.setUpdateTime(r.getUpdateTime());
        return vo;
    }

    /**
     * 列出团队拥有者和管理员ID。
     * @param includeOwner 是否包含拥有者
     */
    private java.util.List<Long> listAdminUserIds(Long teamId, boolean includeOwner) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamMember> mq = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        mq.eq(TeamMember::getTeamId, teamId)
          .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
          .eq(TeamMember::getIsDeleted, 0)
          .in(TeamMember::getTeamRole, includeOwner ? new String[]{TeamMember.ROLE_ADMIN, TeamMember.ROLE_OWNER} : new String[]{TeamMember.ROLE_ADMIN});
        java.util.List<TeamMember> list = teamMemberMapper.selectList(mq);
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (TeamMember tm : list) {
            if (tm.getUserId() != null) ids.add(tm.getUserId());
        }
        return ids;
    }

    /**
     * 获取用户的展示名称：优先昵称，其次用户名；失败时返回字符串形式的ID。
     */
    private String getUserDisplayName(Long userId) {
        if (userId == null) {
            return "-";
        }
        try {
            com.okbug.platform.entity.auth.User u = userMapper.selectById(userId);
            if (u == null) {
                return String.valueOf(userId);
            }
            String name = u.getNickname();
            if (name == null || name.trim().isEmpty()) {
                name = u.getUsername();
            }
            return (name != null && !name.trim().isEmpty()) ? name : String.valueOf(userId);
        } catch (Exception e) {
            return String.valueOf(userId);
        }
    }
}


