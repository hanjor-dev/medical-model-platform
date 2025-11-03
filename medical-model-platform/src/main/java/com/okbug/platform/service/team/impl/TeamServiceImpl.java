/**
 * 团队管理服务实现：后续补充具体业务逻辑
 */
package com.okbug.platform.service.team.impl;

import com.okbug.platform.mapper.team.TeamMapper;
import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.service.team.TeamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.auth.PermissionMapper;
import com.okbug.platform.mapper.auth.UserPermissionMapper;
import com.okbug.platform.vo.team.TeamVO;
import com.okbug.platform.vo.team.TeamMemberVO;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.cache.PermissionCacheService;
import com.okbug.platform.entity.auth.Permission;
import com.okbug.platform.entity.auth.UserPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.concurrent.ThreadLocalRandom;

import com.okbug.platform.service.system.message.NotificationFacade;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    /**
     * 团队管理服务实现
     * 职责：团队创建、成员管理、角色与状态变更、拥有者转移、解散等。
     * 安全：统一使用 ServiceException + ErrorCode，必要处记录业务日志；关键权限变更后清理权限缓存。
     */

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final TeamAccessService teamAccessService;
    
    private final NotificationFacade notificationFacade;
    private final PermissionCacheService permissionCacheService;
    

    @Override
	/**
	 * 按团队码查询未删除的团队。
	 *
	 * 功能：根据唯一 teamCode 查询有效（未逻辑删除）的团队记录。
	 *
	 * @param teamCode 团队码，非空白；为空时直接返回 null
	 * @return 匹配的团队实体；未找到或参数为空返回 null
	 */
    public Team findByTeamCode(String teamCode) {
        log.debug("按团队码查询团队，teamCode={}", teamCode);
        if (teamCode == null || teamCode.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<>();
        query.eq(Team::getTeamCode, teamCode.trim())
             .eq(Team::getIsDeleted, 0)
             .last("limit 1");
        return teamMapper.selectOne(query);
    }

    @Override
	/**
	 * 按主键查询团队。
	 *
	 * @param teamId 团队ID，允许为 null（null 时返回 null）
	 * @return 团队实体；未找到返回 null
	 */
    public Team getById(Long teamId) {
        log.debug("按ID查询团队，teamId={}", teamId);
        if (teamId == null) {
            return null;
        }
        return teamMapper.selectById(teamId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 创建团队并将创建者设为拥有者（OWNER）。
	 *
	 * 事务：是（任何一步失败将回滚）。
	 * 通知：成功后异步推送创建成功通知；尝试授予拥有者菜单权限。
	 *
	 * @param ownerUserId 拥有者用户ID，必填
	 * @param teamName 团队名称，必填
	 * @param description 团队描述，可空
	 * @return 创建成功的团队实体
	 * @throws ServiceException PARAM_MISSING|PARAM_INVALID|OPERATION_TIMEOUT 数据校验或生成团队码失败
	 */
    public Team createTeam(Long ownerUserId, String teamName, String description) {
        log.info("创建团队开始，ownerUserId={}，teamName={}", ownerUserId, teamName);
        if (ownerUserId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "ownerUserId 不能为空");
        }
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "团队名称不能为空");
        }
        Team team = new Team();
        team.setOwnerUserId(ownerUserId);
        team.setTeamName(teamName.trim());
        team.setDescription(description);
        team.setStatus(Team.STATUS_ENABLED);
        // 生成唯一团队码（避免DB非空约束与唯一约束）
        String teamCode = generateUniqueTeamCode();
        team.setTeamCode(teamCode);
        teamMapper.insert(team);
        log.info("团队创建成功，teamId={}，teamCode={}", team.getId(), teamCode);
        // 创建者成为 OWNER 成员
        TeamMember owner = new TeamMember();
        owner.setTeamId(team.getId());
        owner.setUserId(ownerUserId);
        owner.setTeamRole(TeamMember.ROLE_OWNER);
        owner.setStatus(TeamMember.STATUS_ENABLED);
        teamMemberMapper.insert(owner);
        // 推送：通知创建者创建成功
        try {
            notificationFacade.notifyTeamCreated(ownerUserId, team.getTeamName());
        } catch (Exception e) { log.warn("createTeam message push failed: {}", e.getMessage()); }

        // 授予拥有者团队管理相关菜单权限（成员管理/加入申请）
        try {
            grantOwnerMenuPermissions(ownerUserId);
        } catch (Exception e) {
            log.error("grant owner menu permissions failed, userId={}, teamId={}, err={}", ownerUserId, team.getId(), e.getMessage());
        }
        return team;
    }

	/**
	 * 生成唯一团队码。
	 *
	 * 规则：前缀 "TC" + 8 位大写字母/数字；命中唯一索引冲突时重试，最多 10 次。
	 *
	 * @return 唯一团队码字符串
	 * @throws ServiceException OPERATION_TIMEOUT 极小概率多次冲突导致生成失败
	 */
    private String generateUniqueTeamCode() {
        for (int i = 0; i < 10; i++) {
            String code = "TC" + randomAlphaNum(8);
            LambdaQueryWrapper<Team> q = new LambdaQueryWrapper<>();
            q.eq(Team::getTeamCode, code).last("limit 1");
            if (teamMapper.selectCount(q) == 0) {
                log.debug("生成唯一团队码成功，code={}", code);
                return code;
            }
        }
        // 理论上极小概率重试失败
        log.error("生成唯一团队码失败，已重试上限");
        throw new ServiceException(ErrorCode.OPERATION_TIMEOUT, "生成团队码失败，请重试");
    }

    private String randomAlphaNum(int len) {
		/**
		 * 生成指定长度的大写字母/数字随机串。
		 *
		 * @param len 长度，必须大于 0
		 * @return 随机串
		 */
        final char[] dict = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(dict[ThreadLocalRandom.current().nextInt(dict.length)]);
        }
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 更新团队基本信息或状态。
	 *
	 * 事务：是。
	 * 约束：当团队处于禁用状态时，仅允许切换状态本身；其余字段不可修改。
	 *
	 * @param teamId 团队ID
	 * @param teamName 新名称，可空（空则不改）
	 * @param description 描述，可空（空则不改）
	 * @param status 新状态，可空（空则不改）
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED 参数或状态非法
	 */
    public void updateTeam(Long teamId, String teamName, String description, Integer status) {
        log.info("更新团队，teamId={}，teamName={}，status={}", teamId, teamName, status);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        // 允许禁用状态下仅更新状态本身（例如从禁用切回启用）；其余字段在禁用时不允许修改
        boolean isDisabled = team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED;
        if (isDisabled) {
            if (status == null) {
                throw new ServiceException(ErrorCode.TEAM_DISABLED);
            }
            // 仅允许切换状态
            team.setStatus(status);
            teamMapper.updateById(team);
            log.info("团队状态已切换（禁用期间仅允许切换状态），teamId={}，status={}", teamId, status);
            return;
        }
        if (teamName != null && !teamName.trim().isEmpty()) {
            team.setTeamName(teamName.trim());
        }
        if (description != null) {
            team.setDescription(description);
        }
        if (status != null) {
            team.setStatus(status);
        }
        teamMapper.updateById(team);
        log.info("团队更新完成，teamId={}", teamId);
        // 推送：通知所有启用成员团队信息更新
        try {
            java.util.List<Long> memberUserIds = listActiveMemberUserIds(teamId);
            String updatedTeamName = StringUtils.hasText(team.getTeamName()) ? team.getTeamName() : ("ID=" + teamId);
            for (Long uid : memberUserIds) {
                notificationFacade.notifyTeamUpdated(uid, updatedTeamName, team.getOwnerUserId());
            }
        } catch (Exception e) { log.warn("updateTeam message push failed: {}", e.getMessage()); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 逻辑删除团队。
	 *
	 * 事务：是。使用 MyBatis-Plus 逻辑删除语义。
	 *
	 * @param teamId 团队ID
	 * @throws ServiceException TEAM_NOT_FOUND 未找到团队
	 */
    public void softDeleteTeam(Long teamId) {
        log.info("软删除团队，teamId={}", teamId);
        // 使用 MyBatis-Plus 逻辑删除能力，确保统一更新 is_deleted 字段
        int affected = teamMapper.deleteById(teamId);
        if (affected == 0) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 添加成员到团队（或恢复被逻辑删除的成员）。
	 *
	 * 事务：是。
	 * 约束：
	 * - 一个用户同一时间仅能隶属一个未删除团队；
	 * - 禁止通过该接口直接赋予 OWNER 角色；
	 * - 校验团队存在且启用。
	 *
	 * @param teamId 团队ID
	 * @param userId 用户ID
	 * @param role 角色（OWNER/ADMIN/MEMBER），空则默认 MEMBER
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|TEAM_MEMBER_ROLE_INVALID|TEAM_MEMBER_ALREADY_EXISTS|OPERATION_NOT_ALLOWED
	 */
    public void addMember(Long teamId, Long userId, String role) {
        log.info("添加成员，teamId={}，userId={}，role={}", teamId, userId, role);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        String teamRole = StringUtils.hasText(role) ? role : TeamMember.ROLE_MEMBER;
        if (!TeamMember.ROLE_OWNER.equals(teamRole) &&
            !TeamMember.ROLE_ADMIN.equals(teamRole) &&
            !TeamMember.ROLE_MEMBER.equals(teamRole)) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_ROLE_INVALID);
        }
        // 禁止通过普通添加接口直接赋予 OWNER 角色
        if (TeamMember.ROLE_OWNER.equals(teamRole)) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "请使用转移拥有者功能");
        }
        // 全局唯一性：一个用户同一时间只能隶属一个未删除团队
        LambdaQueryWrapper<TeamMember> uq = new LambdaQueryWrapper<>();
        uq.eq(TeamMember::getUserId, userId)
          .eq(TeamMember::getIsDeleted, 0)
          .last("limit 1");
        TeamMember any = teamMemberMapper.selectOne(uq);
        if (any != null && !any.getTeamId().equals(teamId)) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS, "用户已属于其它团队");
        }
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getIsDeleted, 0)
         .last("limit 1");
        TeamMember existing = teamMemberMapper.selectOne(q);
        if (existing != null && existing.getStatus() != null && existing.getStatus() == TeamMember.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
        if (existing == null) {
            // 尝试恢复被逻辑删除的成员关系；否则新建
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TeamMember> revive = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            revive.eq(TeamMember::getTeamId, teamId)
                  .eq(TeamMember::getUserId, userId)
                  .eq(TeamMember::getIsDeleted, 1)
                  .set(TeamMember::getIsDeleted, 0)
                  .set(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                  .set(TeamMember::getTeamRole, teamRole)
                  .set(TeamMember::getJoinedAt, java.time.LocalDateTime.now());
            int revived = teamMemberMapper.update(null, revive);
            if (revived == 0) {
                TeamMember m = new TeamMember();
                m.setTeamId(teamId);
                m.setUserId(userId);
                m.setTeamRole(teamRole);
                m.setStatus(TeamMember.STATUS_ENABLED);
                teamMemberMapper.insert(m);
            }
        } else {
            existing.setTeamRole(teamRole);
            existing.setStatus(TeamMember.STATUS_ENABLED);
            teamMemberMapper.updateById(existing);
        }
        log.info("添加成员完成，teamId={}，userId={}，role={}", teamId, userId, teamRole);
        // 推送：通知被添加成员
        try {
            Team t = teamMapper.selectById(teamId);
            notificationFacade.notifyMemberAdded(userId, t != null ? t.getTeamName() : ("ID=" + teamId), teamRole, t != null ? t.getOwnerUserId() : null);
        } catch (Exception e) { log.warn("addMember message push failed: {}", e.getMessage()); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 更新团队成员角色/状态。
	 *
	 * 事务：是。
	 * 约束：
	 * - 不允许更改/禁用 OWNER；
	 * - 降级/禁用最后一名管理员时需确保仍有 OWNER 或其他管理员。
	 *
	 * @param teamId 团队ID
	 * @param userId 用户ID
	 * @param role 新角色，可空
	 * @param status 新状态，可空
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|TEAM_MEMBER_NOT_FOUND|OPERATION_NOT_ALLOWED|TEAM_MEMBER_ROLE_INVALID
	 */
    public void updateMember(Long teamId, Long userId, String role, Integer status) {
        log.info("更新成员，teamId={}，userId={}，role={}，status={}", teamId, userId, role, status);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getIsDeleted, 0)
         .last("limit 1");
        TeamMember m = teamMemberMapper.selectOne(q);
        if (m == null) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }
        // 阻止对 OWNER 的降级或禁用
        if (TeamMember.ROLE_OWNER.equals(m.getTeamRole())) {
            if (status != null && status == TeamMember.STATUS_DISABLED) {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能禁用团队拥有者");
            }
            if (org.springframework.util.StringUtils.hasText(role) && !TeamMember.ROLE_OWNER.equals(role)) {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能更改团队拥有者角色");
            }
        }

        // 如果尝试将管理员降为成员或禁用，需确保仍至少保留一名管理员或OWNER
        boolean roleWillChange = org.springframework.util.StringUtils.hasText(role) && !role.equals(m.getTeamRole());
        boolean willDisable = status != null && status == TeamMember.STATUS_DISABLED;
        if (TeamMember.ROLE_ADMIN.equals(m.getTeamRole()) && (roleWillChange || willDisable)) {
            // 统计当前启用的管理员数量（不含OWNER）
            LambdaQueryWrapper<TeamMember> countQ = new LambdaQueryWrapper<>();
            countQ.eq(TeamMember::getTeamId, teamId)
                  .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                  .eq(TeamMember::getTeamRole, TeamMember.ROLE_ADMIN);
            long adminEnabledCount = teamMemberMapper.selectCount(countQ);

            // 统计是否存在启用的OWNER（理论上至少1）
            LambdaQueryWrapper<TeamMember> ownerQ = new LambdaQueryWrapper<>();
            ownerQ.eq(TeamMember::getTeamId, teamId)
                  .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                  .eq(TeamMember::getTeamRole, TeamMember.ROLE_OWNER)
                  .last("limit 1");
            TeamMember owner = teamMemberMapper.selectOne(ownerQ);

            String targetRole = roleWillChange ? role : m.getTeamRole();
            boolean isDowngrade = TeamMember.ROLE_ADMIN.equals(m.getTeamRole()) && TeamMember.ROLE_MEMBER.equals(targetRole);
            boolean isDisable = willDisable;

            if (adminEnabledCount <= 1 && (isDowngrade || isDisable)) {
                // 如果没有OWNER可覆盖管理能力，则禁止导致无管理者
                if (owner == null) {
                    throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "至少保留一名管理员或拥有者");
                }
                // 如果有OWNER，可以允许0管理员，但仍允许此操作
                // 但要确保不是对OWNER操作，上面已阻止
            }
        }
        if (StringUtils.hasText(role)) {
            if (!TeamMember.ROLE_OWNER.equals(role) &&
                !TeamMember.ROLE_ADMIN.equals(role) &&
                !TeamMember.ROLE_MEMBER.equals(role)) {
                throw new ServiceException(ErrorCode.TEAM_MEMBER_ROLE_INVALID);
            }
            // 禁止通过普通更新接口将任意成员设置为 OWNER
            if (TeamMember.ROLE_OWNER.equals(role) && !TeamMember.ROLE_OWNER.equals(m.getTeamRole())) {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "请使用转移拥有者功能");
            }
            m.setTeamRole(role);
        }
        if (status != null) {
            m.setStatus(status);
        }
        teamMemberMapper.updateById(m);
        log.info("更新成员完成，teamId={}，userId={}，role={}，status={}", teamId, userId, m.getTeamRole(), m.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 从团队移除成员（逻辑删除）。
	 *
	 * 事务：是。
	 * 约束：
	 * - 不允许移除 OWNER；
	 * - 若为最后一名启用管理员且无 OWNER 启用，则禁止移除。
	 *
	 * @param teamId 团队ID
	 * @param userId 用户ID
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|OPERATION_NOT_ALLOWED
	 */
    public void removeMember(Long teamId, Long userId) {
        log.info("移除成员，teamId={}，userId={}", teamId, userId);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getUserId, userId)
         .eq(TeamMember::getIsDeleted, 0)
         .last("limit 1");
        TeamMember m = teamMemberMapper.selectOne(q);
        if (m == null) { return; }
        // 不允许移除OWNER
        if (TeamMember.ROLE_OWNER.equals(m.getTeamRole())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能移除团队拥有者");
        }
        // 如果这是最后一个启用的管理员，且无OWNER启用，禁止移除
        if (TeamMember.ROLE_ADMIN.equals(m.getTeamRole()) && m.getStatus() != null && m.getStatus() == TeamMember.STATUS_ENABLED) {
            LambdaQueryWrapper<TeamMember> adminQ = new LambdaQueryWrapper<>();
            adminQ.eq(TeamMember::getTeamId, teamId)
                  .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                  .eq(TeamMember::getTeamRole, TeamMember.ROLE_ADMIN)
                  .eq(TeamMember::getIsDeleted, 0);
            long adminEnabledCount = teamMemberMapper.selectCount(adminQ);

            LambdaQueryWrapper<TeamMember> ownerQ = new LambdaQueryWrapper<>();
            ownerQ.eq(TeamMember::getTeamId, teamId)
                  .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
                  .eq(TeamMember::getTeamRole, TeamMember.ROLE_OWNER)
                  .eq(TeamMember::getIsDeleted, 0)
                  .last("limit 1");
            TeamMember owner = teamMemberMapper.selectOne(ownerQ);

            if (adminEnabledCount <= 1 && owner == null) {
                throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "至少保留一名管理员或拥有者");
            }
        }
        // 逻辑删除，以便后续可“复活”避免唯一键冲突
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TeamMember> del = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        del.eq(TeamMember::getId, m.getId())
           .set(TeamMember::getIsDeleted, 1)
           .set(TeamMember::getStatus, TeamMember.STATUS_DISABLED);
        teamMemberMapper.update(null, del);
        log.info("成员已逻辑删除，teamId={}，userId={}", teamId, userId);
        // 推送：通知被移除成员
        try {
            Team t = teamMapper.selectById(teamId);
            notificationFacade.notifyMemberRemoved(userId, t != null ? t.getTeamName() : ("ID=" + teamId), t != null ? t.getOwnerUserId() : null);
        } catch (Exception e) { log.warn("removeMember message push failed: {}", e.getMessage()); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 转移团队拥有者。
	 *
	 * 事务：是。
	 * 约束：仅当前 OWNER 可发起；目标必须是启用状态的 ADMIN，将被晋升为 OWNER，原 OWNER 自动降为 ADMIN。
	 *
	 * @param teamId 团队ID
	 * @param fromOwnerUserId 原拥有者用户ID
	 * @param toUserId 目标用户ID（必须是管理员）
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN|TEAM_MEMBER_NOT_FOUND|OPERATION_NOT_ALLOWED
	 */
    public void transferOwner(Long teamId, Long fromOwnerUserId, Long toUserId) {
        log.info("转移团队拥有者，teamId={}，from={}，to={}", teamId, fromOwnerUserId, toUserId);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        // 校验发起者确为当前OWNER
        if (team.getOwnerUserId() == null || !team.getOwnerUserId().equals(fromOwnerUserId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "只有当前拥有者可以转移拥有者");
        }
        // 目标成员必须存在且启用，且其当前团队角色必须是 ADMIN（仅允许管理员晋升为 OWNER）
        LambdaQueryWrapper<TeamMember> toQ = new LambdaQueryWrapper<>();
        toQ.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, toUserId).last("limit 1");
        TeamMember to = teamMemberMapper.selectOne(toQ);
        if (to == null || to.getStatus() == null || to.getStatus() != TeamMember.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_NOT_FOUND, "目标用户不是启用成员");
        }
        if (!TeamMember.ROLE_ADMIN.equals(to.getTeamRole())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "仅允许将管理员转为拥有者");
        }
        // 原OWNER记录
        LambdaQueryWrapper<TeamMember> fromQ = new LambdaQueryWrapper<>();
        fromQ.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, fromOwnerUserId).last("limit 1");
        TeamMember from = teamMemberMapper.selectOne(fromQ);
        if (from == null || !TeamMember.ROLE_OWNER.equals(from.getTeamRole())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "当前用户不是拥有者");
        }
        // 更新团队拥有者ID
        team.setOwnerUserId(toUserId);
        teamMapper.updateById(team);
        // 目标成员设为 OWNER
        to.setTeamRole(TeamMember.ROLE_OWNER);
        to.setStatus(TeamMember.STATUS_ENABLED);
        teamMemberMapper.updateById(to);
        // 原OWNER降为 ADMIN（若当前不是 OWNER，也会在前面被拒绝）
        from.setTeamRole(TeamMember.ROLE_ADMIN);
        from.setStatus(TeamMember.STATUS_ENABLED);
        teamMemberMapper.updateById(from);
        log.info("拥有者转移完成，teamId={}，newOwner={}", teamId, toUserId);
        // 推送：通知新旧拥有者
        try {
            Team t = teamMapper.selectById(teamId);
            String teamName = t != null ? t.getTeamName() : ("ID=" + teamId);
            notificationFacade.notifyOwnerPromoted(toUserId, teamName, fromOwnerUserId);
            String toName = getUserDisplayName(toUserId);
            notificationFacade.notifyOwnerTransferredNoticeOldOwner(fromOwnerUserId, teamName, toName, fromOwnerUserId);
        } catch (Exception e) { log.warn("transferOwner message push failed: {}", e.getMessage()); }

        // 撤销原拥有者的团队管理菜单权限，并授予新拥有者
        try {
            revokeOwnerMenuPermissions(fromOwnerUserId);
        } catch (Exception e) {
            log.error("revoke owner menu permissions failed, userId={}, teamId={}, err={}", fromOwnerUserId, teamId, e.getMessage());
        }
        try {
            grantOwnerMenuPermissions(toUserId);
        } catch (Exception e) {
            log.error("grant new owner menu permissions failed, userId={}, teamId={}, err={}", toUserId, teamId, e.getMessage());
        }
    }

	/**
	 * 分页查询团队列表（按操作者权限过滤）。
	 *
	 * 权限：
	 * - SUPER_ADMIN/ADMIN：可查看所有未删除团队；
	 * - 其他角色：仅返回其所在团队；若无成员关系，返回空分页。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param pageNum 页码（默认1）
	 * @param pageSize 页大小（默认10）
	 * @param keyword 模糊搜索关键字（团队名）
	 * @return 团队分页（VO）
	 */
    @Override
    public IPage<TeamVO> listTeams(Long operatorUserId, Integer pageNum, Integer pageSize, String keyword) {
        log.debug("分页查询团队，operator={}，pageNum={}，pageSize={}，keyword='{}'", operatorUserId, pageNum, pageSize, keyword);
        Page<Team> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Team> q = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            q.like(Team::getTeamName, keyword);
        }
        q.eq(Team::getIsDeleted, 0);
        boolean isSuperAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("SUPER_ADMIN");
        boolean isAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("ADMIN");
        // 仅返回未删除团队
        q.eq(Team::getIsDeleted, 0);
        if (!isSuperAdmin && !isAdmin) {
            LambdaQueryWrapper<TeamMember> mq = new LambdaQueryWrapper<>();
            mq.eq(TeamMember::getUserId, operatorUserId)
              .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
              .eq(TeamMember::getIsDeleted, 0)
              .last("limit 1");
            TeamMember m = teamMemberMapper.selectOne(mq);
            if (m != null) {
                q.eq(Team::getId, m.getTeamId());
            } else {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
        }
        IPage<Team> teamPage = teamMapper.selectPage(page, q);
        return teamPage.convert(this::toVO);
    }

	/**
	 * 查询团队详情（成员可见）。
	 *
	 * 权限：仅团队启用成员可见。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @return 团队详情（包含当前用户在团队的角色等摘要）
	 * @throws ServiceException FORBIDDEN|TEAM_NOT_FOUND 权限或资源不存在
	 */
    @Override
    public TeamVO getTeamDetail(Long operatorUserId, Long teamId) {
        log.debug("获取团队详情，operator={}，teamId={}", operatorUserId, teamId);
        LambdaQueryWrapper<TeamMember> mq = new LambdaQueryWrapper<>();
        mq.eq(TeamMember::getTeamId, teamId)
          .eq(TeamMember::getUserId, operatorUserId)
          .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
          .eq(TeamMember::getIsDeleted, 0)
          .last("limit 1");
        TeamMember membership = teamMemberMapper.selectOne(mq);
        if (membership == null) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权访问团队");
        }
        Team team = getById(teamId);
        if (team == null || (team.getIsDeleted() != null && team.getIsDeleted() == 1)) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        TeamVO vo = toVO(team);
        // 填充当前用户在团队的角色
        vo.setMyTeamRole(membership.getTeamRole());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全更新团队：在权限与状态校验通过后委托到 {@link #updateTeam(Long, String, String, Integer)}。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @param teamName 名称，可空
	 * @param description 描述，可空
	 * @param status 状态，可空
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void updateTeamSecure(Long operatorUserId, Long teamId, String teamName, String description, Integer status) {
        log.info("安全更新团队，operator={}，teamId={}", operatorUserId, teamId);
        // 先给出更明确的业务提示：团队被禁用时，阻止修改并返回 TEAM_DISABLED
        Team team = getById(teamId);
        if (team == null || (team.getIsDeleted() != null && team.getIsDeleted() == 1)) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED, "团队被禁用，暂不支持修改");
        }
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权操作团队");
        }
        updateTeam(teamId, teamName, description, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全软删除团队：权限与状态校验通过后执行逻辑删除。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void softDeleteTeamSecure(Long operatorUserId, Long teamId) {
        log.info("安全软删除团队，operator={}，teamId={}", operatorUserId, teamId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED, "团队被禁用，暂无法操作");
        }
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权删除团队");
        }
        softDeleteTeam(teamId);
    }

    @Override
	/**
	 * 通过团队码预览团队摘要信息。
	 *
	 * @param teamCode 团队码
	 * @return 团队视图对象
	 * @throws ServiceException PARAM_INVALID|TEAM_CODE_INVALID|TEAM_DISABLED
	 */
    public TeamVO previewByCode(String teamCode) {
        log.debug("预览团队，teamCode={}", teamCode);
        if (!org.springframework.util.StringUtils.hasText(teamCode)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "团队码不能为空");
        }
        Team t = findByTeamCode(teamCode.trim());
        if (t == null || (t.getIsDeleted() != null && t.getIsDeleted() == 1)) {
            throw new ServiceException(ErrorCode.TEAM_CODE_INVALID);
        }
        if (t.getStatus() != null && t.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        return toVO(t);
    }

    @Override
	/**
	 * 分页查询团队成员。
	 *
	 * 权限：需要具备团队管理查看权限。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param role 按角色过滤，可空
	 * @param status 按状态过滤，可空
	 * @return 成员分页（VO）
	 * @throws ServiceException FORBIDDEN 权限不足
	 */
    public IPage<TeamMemberVO> listMembers(Long operatorUserId, Long teamId, Integer pageNum, Integer pageSize, String role, Integer status) {
        log.debug("分页查询成员，operator={}，teamId={}，pageNum={}，pageSize={}，role={}，status={}", operatorUserId, teamId, pageNum, pageSize, role, status);
        if (!teamAccessService.canViewTeamForManagement(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权查看团队成员");
        }
        Page<TeamMember> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getIsDeleted, 0);
        if (status != null) {
            q.eq(TeamMember::getStatus, status);
        }
        if (org.springframework.util.StringUtils.hasText(role)) {
            q.eq(TeamMember::getTeamRole, role);
        }
        IPage<TeamMember> memberPage = teamMemberMapper.selectPage(page, q);
        Page<TeamMemberVO> voPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
        java.util.List<TeamMember> records = memberPage.getRecords();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (TeamMember tm : records) {
            if (tm.getUserId() != null) userIds.add(tm.getUserId());
        }
        java.util.Map<Long, com.okbug.platform.entity.auth.User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            java.util.List<com.okbug.platform.entity.auth.User> users = userMapper.selectBatchIds(userIds);
            for (com.okbug.platform.entity.auth.User u : users) {
                userMap.put(u.getId(), u);
            }
        }
        java.util.List<TeamMemberVO> voRecords = new java.util.ArrayList<>();
        for (TeamMember tm : records) {
            TeamMemberVO vo = new TeamMemberVO();
            vo.setUserId(tm.getUserId());
            vo.setTeamRole(tm.getTeamRole());
            vo.setStatus(tm.getStatus());
            vo.setJoinedAt(tm.getJoinedAt());
            com.okbug.platform.entity.auth.User u = userMap.get(tm.getUserId());
            if (u != null) {
                vo.setId(u.getId());
                vo.setUsername(u.getUsername());
                vo.setNickname(u.getNickname());
                vo.setEmail(u.getEmail());
                vo.setRole(u.getRole());
                vo.setUserStatus(u.getStatus());
                vo.setCreateTime(u.getCreateTime());
                vo.setLastLoginTime(u.getLastLoginTime());
            }
            voRecords.add(vo);
        }
        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全添加成员：校验团队状态与操作者权限后委托到 {@link #addMember(Long, Long, String)}。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @param userId 被添加用户ID
	 * @param role 角色，空则默认 MEMBER
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void addMemberSecure(Long operatorUserId, Long teamId, Long userId, String role) {
        log.info("安全添加成员，operator={}，teamId={}，userId={}", operatorUserId, teamId, userId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权添加成员");
        }
        addMember(teamId, userId, role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全更新成员：校验团队状态与操作者权限后委托到 {@link #updateMember(Long, Long, String, Integer)}。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @param userId 成员用户ID
	 * @param role 角色，可空
	 * @param status 状态，可空
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void updateMemberSecure(Long operatorUserId, Long teamId, Long userId, String role, Integer status) {
        log.info("安全更新成员，operator={}，teamId={}，userId={}", operatorUserId, teamId, userId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权更新成员");
        }
        updateMember(teamId, userId, role, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全移除成员：校验团队状态与操作者权限后委托到 {@link #removeMember(Long, Long)}。
	 *
	 * @param operatorUserId 操作者用户ID
	 * @param teamId 团队ID
	 * @param userId 成员用户ID
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void removeMemberSecure(Long operatorUserId, Long teamId, Long userId) {
        log.info("安全移除成员，operator={}，teamId={}，userId={}", operatorUserId, teamId, userId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权移除成员");
        }
        removeMember(teamId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 安全转移拥有者：校验操作者确为当前 OWNER 后委托到 {@link #transferOwner(Long, Long, Long)}。
	 *
	 * @param operatorUserId 操作者（当前拥有者）
	 * @param teamId 团队ID
	 * @param toUserId 目标用户ID（管理员）
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void transferOwnerSecure(Long operatorUserId, Long teamId, Long toUserId) {
        log.info("安全转移拥有者，operator={}，teamId={}，to={}", operatorUserId, teamId, toUserId);
        // 只有当前OWNER可执行：在内部再校验一次
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED);
        }
        if (team.getOwnerUserId() == null || !team.getOwnerUserId().equals(operatorUserId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "只有当前拥有者可以转移拥有者");
        }
        transferOwner(teamId, operatorUserId, toUserId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
	/**
	 * 成员主动退出团队。
	 *
	 * 事务：是。OWNER 不允许退出。
	 *
	 * @param operatorUserId 成员用户ID
	 * @param teamId 团队ID
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_MEMBER_NOT_FOUND|OPERATION_NOT_ALLOWED
	 */
    public void exitTeamSelf(Long operatorUserId, Long teamId) {
        log.info("成员自行退出团队，operator={}，teamId={}", operatorUserId, teamId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        // OWNER 不允许退出
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId)
         .eq(TeamMember::getUserId, operatorUserId)
         .eq(TeamMember::getIsDeleted, 0)
         .last("limit 1");
        TeamMember membership = teamMemberMapper.selectOne(q);
        if (membership == null) {
            throw new ServiceException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }
        if (TeamMember.ROLE_OWNER.equals(membership.getTeamRole())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "拥有者不能直接退出，请先转移拥有者或解散团队");
        }
        // 逻辑删除自身成员关系
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TeamMember> del = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        del.eq(TeamMember::getId, membership.getId())
           .set(TeamMember::getIsDeleted, 1)
           .set(TeamMember::getStatus, TeamMember.STATUS_DISABLED);
        teamMemberMapper.update(null, del);
        // 推送：通知团队拥有者和管理员有成员退出
        try {
            Team t = teamMapper.selectById(teamId);
            java.util.List<Long> admins = listAdminUserIds(teamId, true);
            String teamName = t != null ? t.getTeamName() : ("ID=" + teamId);
            String leaverName = getUserDisplayName(operatorUserId);
            for (Long adminId : admins) {
                notificationFacade.notifyMemberExitedToAdmin(adminId, teamName, leaverName, operatorUserId);
            }
        } catch (Exception e) { log.warn("exitTeamSelf message push failed: {}", e.getMessage()); }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
	/**
	 * 解散团队（仅拥有者）。
	 *
	 * 事务：是。逻辑删除团队并禁用/逻辑删除所有成员关系，随后通知所有成员并撤销拥有者菜单权限。
	 *
	 * @param operatorUserId 拥有者用户ID
	 * @param teamId 团队ID
	 * @throws ServiceException TEAM_NOT_FOUND|TEAM_DISABLED|FORBIDDEN
	 */
    public void dissolveTeamSecure(Long operatorUserId, Long teamId) {
        log.info("解散团队（安全），operator={}，teamId={}", operatorUserId, teamId);
        Team team = getById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (team.getStatus() != null && team.getStatus() != Team.STATUS_ENABLED) {
            throw new ServiceException(ErrorCode.TEAM_DISABLED, "团队被禁用，暂无法操作");
        }
        if (team.getOwnerUserId() == null || !team.getOwnerUserId().equals(operatorUserId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "只有拥有者可以解散团队");
        }
        // 在解散前查询所有启用成员，供消息推送
        java.util.List<Long> memberIds = listActiveMemberUserIds(teamId);
        // 软删除团队
        softDeleteTeam(teamId);
        // 禁用/逻辑删除所有成员关系
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TeamMember> delAll = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        delAll.eq(TeamMember::getTeamId, teamId)
              .eq(TeamMember::getIsDeleted, 0)
              .set(TeamMember::getIsDeleted, 1)
              .set(TeamMember::getStatus, TeamMember.STATUS_DISABLED);
        teamMemberMapper.update(null, delAll);
        // 推送：通知原团队所有成员团队已解散
        try {
            String teamName = team.getTeamName() != null ? team.getTeamName() : ("ID=" + teamId);
            for (Long uid : memberIds) {
                notificationFacade.notifyTeamDissolved(uid, teamName, operatorUserId);
            }
        } catch (Exception e) { log.warn("dissolveTeam message push failed: {}", e.getMessage()); }

        // 撤销拥有者的团队管理菜单权限
        try {
            revokeOwnerMenuPermissions(operatorUserId);
        } catch (Exception e) {
            log.error("revoke owner menu permissions on dissolve failed, userId={}, teamId={}, err={}", operatorUserId, teamId, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	/**
	 * 超级管理员设置团队状态（启用/禁用）。
	 *
	 * 权限：仅 SUPER_ADMIN。
	 *
	 * @param operatorUserId 操作者用户ID（必须具备 SUPER_ADMIN 角色）
	 * @param teamId 团队ID
	 * @param status 目标状态（ENABLED/DISABLED）
	 * @throws ServiceException FORBIDDEN|PARAM_INVALID|TEAM_NOT_FOUND
	 */
    public void setTeamStatusBySuperAdmin(Long operatorUserId, Long teamId, Integer status) {
        // 要求调用方已通过 SUPER_ADMIN 角色校验；此处仍做一次防御性校验
        boolean isSuperAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "仅限超级管理员");
        }
        if (status == null || (status != Team.STATUS_ENABLED && status != Team.STATUS_DISABLED)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "非法的团队状态");
        }
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new ServiceException(ErrorCode.TEAM_NOT_FOUND);
        }
        Integer oldStatus = team.getStatus();
        team.setStatus(status);
        teamMapper.updateById(team);
        // 根据状态切换方向通知团队拥有者
        try {
            String teamName = team.getTeamName() != null ? team.getTeamName() : ("ID=" + teamId);
            if (oldStatus != null && oldStatus == Team.STATUS_ENABLED && status == Team.STATUS_DISABLED) {
                notificationFacade.notifyTeamDisabledToOwner(team.getOwnerUserId(), teamName, operatorUserId);
            } else if (oldStatus != null && oldStatus == Team.STATUS_DISABLED && status == Team.STATUS_ENABLED) {
                notificationFacade.notifyTeamEnabledToOwner(team.getOwnerUserId(), teamName, operatorUserId);
            }
        } catch (Exception e) { log.warn("setTeamStatusBySuperAdmin notify failed: {}", e.getMessage()); }
    }

	/**
	 * 将团队实体转换为摘要视图对象。
	 *
	 * 包含成员数/管理员数统计与拥有者名称填充。
	 *
	 * @param t 团队实体
	 * @return 视图对象
	 */
	private TeamVO toVO(Team t) {
        TeamVO vo = new TeamVO();
        vo.setId(t.getId());
        vo.setTeamName(t.getTeamName());
        vo.setTeamCode(t.getTeamCode());
        vo.setDescription(t.getDescription());
        vo.setOwnerUserId(t.getOwnerUserId());
        vo.setStatus(t.getStatus());
        // 审计字段
        vo.setCreateTime(t.getCreateTime());
        vo.setUpdateTime(t.getUpdateTime());
        // 填充拥有者用户名/昵称
        if (t.getOwnerUserId() != null) {
            com.okbug.platform.entity.auth.User owner = userMapper.selectById(t.getOwnerUserId());
            if (owner != null) {
                String name = owner.getNickname();
                if (!org.springframework.util.StringUtils.hasText(name)) {
                    name = owner.getUsername();
                }
                vo.setOwnerName(name);
            }
        }
        // 统计成员数量与管理员数量（启用状态）
        if (t.getId() != null) {
            LambdaQueryWrapper<TeamMember> mq1 = new LambdaQueryWrapper<>();
            mq1.eq(TeamMember::getTeamId, t.getId()).eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED).eq(TeamMember::getIsDeleted, 0);
            int memberCount = Math.toIntExact(teamMemberMapper.selectCount(mq1));
            vo.setMemberCount(memberCount);

            LambdaQueryWrapper<TeamMember> mq2 = new LambdaQueryWrapper<>();
            mq2.eq(TeamMember::getTeamId, t.getId()).eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED).eq(TeamMember::getTeamRole, TeamMember.ROLE_ADMIN).eq(TeamMember::getIsDeleted, 0);
            int adminCount = Math.toIntExact(teamMemberMapper.selectCount(mq2));
            vo.setAdminCount(adminCount);
        }
        return vo;
    }

	/**
	 * 列出团队内所有启用成员的用户ID。
	 *
	 * @param teamId 团队ID
	 * @return 成员用户ID列表（仅启用且未删除）
	 */
    private java.util.List<Long> listActiveMemberUserIds(Long teamId) {
        LambdaQueryWrapper<TeamMember> mq = new LambdaQueryWrapper<>();
        mq.eq(TeamMember::getTeamId, teamId)
          .eq(TeamMember::getStatus, TeamMember.STATUS_ENABLED)
          .eq(TeamMember::getIsDeleted, 0);
        java.util.List<TeamMember> list = teamMemberMapper.selectList(mq);
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (TeamMember tm : list) {
            if (tm.getUserId() != null) ids.add(tm.getUserId());
        }
        return ids;
    }

	/**
	 * 列出团队拥有者和管理员用户ID。
	 *
	 * @param teamId 团队ID
	 * @param includeOwner 是否包含拥有者
	 * @return 用户ID列表
	 */
    private java.util.List<Long> listAdminUserIds(Long teamId, boolean includeOwner) {
        LambdaQueryWrapper<TeamMember> mq = new LambdaQueryWrapper<>();
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
	 * 获取用户展示名称。
	 *
	 * 优先使用昵称，其次用户名；均缺失或异常时返回 ID 字符串。
	 *
	 * @param userId 用户ID
	 * @return 展示名称
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
            if (!org.springframework.util.StringUtils.hasText(name)) {
                name = u.getUsername();
            }
            return org.springframework.util.StringUtils.hasText(name) ? name : String.valueOf(userId);
        } catch (Exception e) {
            return String.valueOf(userId);
        }
    }

	/**
	 * 授予拥有者“成员管理”和“加入申请”菜单权限。
	 *
	 * 行为：不存在则插入；被逻辑删除则恢复为允许。
	 * 在事务提交后清理权限缓存。
	 *
	 * @param userId 拥有者用户ID
	 */
    private void grantOwnerMenuPermissions(Long userId) {
        if (userId == null) return;
        java.util.List<String> codes = java.util.Arrays.asList("team:members", "team:join-requests");
        LambdaQueryWrapper<Permission> pq = new LambdaQueryWrapper<>();
        pq.in(Permission::getPermissionCode, codes)
          .eq(Permission::getStatus, Permission.STATUS_ENABLED)
          .eq(Permission::getIsDeleted, 0);
        java.util.List<Permission> perms = permissionMapper.selectList(pq);
        if (perms == null || perms.isEmpty()) {
            return;
        }
        for (Permission p : perms) {
            if (p == null || p.getId() == null) continue;
            LambdaQueryWrapper<UserPermission> upq = new LambdaQueryWrapper<>();
            upq.eq(UserPermission::getUserId, userId)
               .eq(UserPermission::getPermissionId, p.getId())
               .last("limit 1");
            UserPermission exist = userPermissionMapper.selectOne(upq);
            if (exist == null) {
                UserPermission up = new UserPermission();
                up.setUserId(userId);
                up.setPermissionId(p.getId());
                up.setGrantType(UserPermission.GRANT_ALLOW);
                try {
                    userPermissionMapper.insert(up);
                } catch (DuplicateKeyException ignore) {
                }
            } else if (exist.getIsDeleted() != null && exist.getIsDeleted() == 1) {
                LambdaUpdateWrapper<UserPermission> revive = new LambdaUpdateWrapper<>();
                revive.eq(UserPermission::getId, exist.getId())
                      .set(UserPermission::getIsDeleted, 0)
                      .set(UserPermission::getGrantType, UserPermission.GRANT_ALLOW);
                userPermissionMapper.update(null, revive);
            }
        }
        // 清理缓存并在事务提交后推送权限变更
        notifyUserPermissionChanged(userId);
    }

	/**
	 * 撤销拥有者“成员管理”和“加入申请”菜单权限。
	 *
	 * 行为：物理删除用户-权限关联。
	 * 在事务提交后清理权限缓存。
	 *
	 * @param userId 拥有者用户ID
	 */
    private void revokeOwnerMenuPermissions(Long userId) {
        if (userId == null) return;
        java.util.List<String> codes = java.util.Arrays.asList("team:members", "team:join-requests");
        LambdaQueryWrapper<Permission> pq = new LambdaQueryWrapper<>();
        pq.in(Permission::getPermissionCode, codes)
          .eq(Permission::getIsDeleted, 0);
        java.util.List<Permission> perms = permissionMapper.selectList(pq);
        if (perms == null || perms.isEmpty()) {
            return;
        }
        java.util.List<Long> pids = new java.util.ArrayList<>();
        for (Permission p : perms) { if (p != null && p.getId() != null) pids.add(p.getId()); }
        if (pids.isEmpty()) return;

        // 改为物理删除用户-权限关联
        userPermissionMapper.deleteByUserAndPermissionIdsPhysical(userId, pids);

        // 清理缓存并在事务提交后推送权限变更
        notifyUserPermissionChanged(userId);
    }

	/**
	 * 通知用户权限发生变更：在事务提交后清理权限缓存。
	 *
	 * @param userId 用户ID
	 */
	private void notifyUserPermissionChanged(Long userId) {
        // 前端在接口返回成功后主动刷新权限，此处只需确保在事务提交后清理权限缓存
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        permissionCacheService.clearUserPermissions(userId);
                    } catch (Exception e) {
                        log.warn("clear user permission cache failed, userId={}, err={}", userId, e.getMessage());
                    }
                }
            });
        } else {
            try {
                permissionCacheService.clearUserPermissions(userId);
            } catch (Exception e) {
                log.warn("clear user permission cache failed, userId={}, err={}", userId, e.getMessage());
            }
        }
    }
}


