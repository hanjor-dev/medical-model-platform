package com.okbug.platform.config.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.entity.team.TeamMember;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.team.TeamMapper;
import com.okbug.platform.mapper.team.TeamMemberMapper;
import com.okbug.platform.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 一次性迁移：将基于 parentUserId 的子账号体系迁移为团队模型。
 * - 为每个存在子账号的管理员创建一个团队（若不存在），命名为 {adminUsername}-team
 * - 将其子账号加入该团队并赋予 MEMBER 角色；管理员赋予 OWNER 角色
 * - 幂等：重复运行不会重复插入（依赖唯一索引与存在性判断）
 * - 通过 SystemConfigKeys.MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED 控制是否执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubAccountToTeamMigrationRunner implements CommandLineRunner {

    private final SystemConfigService systemConfigService;
    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public void run(String... args) {
        boolean enabled = Boolean.parseBoolean(systemConfigService.getConfigValue(
                SystemConfigKeys.MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED,
                SystemConfigKeys.getDefaultValue(SystemConfigKeys.MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED))
        );
        if (!enabled) {
            return;
        }
        try {
            executeMigration();
            log.info("SubAccountToTeamMigrationRunner finished");
        } catch (Exception e) {
            log.warn("SubAccountToTeamMigrationRunner error: {}", e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected void executeMigration() {
        // 1) 找出拥有子账号的管理员
        LambdaQueryWrapper<User> adminsQ = new LambdaQueryWrapper<>();
        adminsQ.eq(User::getRole, User.ROLE_ADMIN).eq(User::getIsDeleted, 0);
        List<User> admins = userMapper.selectList(adminsQ);
        if (admins == null || admins.isEmpty()) {
            return;
        }
        for (User admin : admins) {
            // 该管理员是否有子账号
            LambdaQueryWrapper<User> childrenQ = new LambdaQueryWrapper<>();
            childrenQ.eq(User::getParentUserId, admin.getId()).eq(User::getIsDeleted, 0);
            List<User> children = userMapper.selectList(childrenQ);
            if (children == null || children.isEmpty()) {
                continue;
            }

            // 2) 确保团队存在
            Team team = findOrCreateTeamForAdmin(admin);

            // 3) 确保 OWNER 成员存在
            ensureMember(team.getId(), admin.getId(), TeamMember.ROLE_OWNER);

            // 4) 将子账号加入为 MEMBER
            for (User sub : children) {
                ensureMember(team.getId(), sub.getId(), TeamMember.ROLE_MEMBER);
            }
        }
    }

    private Team findOrCreateTeamForAdmin(User admin) {
        String defaultTeamName = admin.getUsername() + "-team";
        LambdaQueryWrapper<Team> q = new LambdaQueryWrapper<>();
        q.eq(Team::getOwnerUserId, admin.getId()).eq(Team::getIsDeleted, 0).last("limit 1");
        Team team = teamMapper.selectOne(q);
        if (team != null) {
            return team;
        }
        Team t = new Team();
        t.setTeamName(defaultTeamName);
        t.setDescription("Auto created by migration from sub-account model");
        t.setOwnerUserId(admin.getId());
        t.setStatus(Team.STATUS_ENABLED);
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        try {
            teamMapper.insert(t);
            return t;
        } catch (Exception e) {
            // 可能由于名称唯一索引冲突被并发创建，回查一次
            LambdaQueryWrapper<Team> q2 = new LambdaQueryWrapper<>();
            q2.eq(Team::getOwnerUserId, admin.getId()).eq(Team::getTeamName, defaultTeamName).last("limit 1");
            Team existed = teamMapper.selectOne(q2);
            if (existed != null) return existed;
            throw e;
        }
    }

    private void ensureMember(Long teamId, Long userId, String role) {
        LambdaQueryWrapper<TeamMember> q = new LambdaQueryWrapper<>();
        q.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId).last("limit 1");
        TeamMember m = teamMemberMapper.selectOne(q);
        if (m == null) {
            TeamMember nm = new TeamMember();
            nm.setTeamId(teamId);
            nm.setUserId(userId);
            nm.setTeamRole(role);
            nm.setStatus(TeamMember.STATUS_ENABLED);
            nm.setJoinedAt(LocalDateTime.now());
            teamMemberMapper.insert(nm);
        } else {
            boolean needUpdate = false;
            if (m.getStatus() == null || m.getStatus() != TeamMember.STATUS_ENABLED) {
                m.setStatus(TeamMember.STATUS_ENABLED);
                needUpdate = true;
            }
            if (role != null && !role.equals(m.getTeamRole())) {
                m.setTeamRole(role);
                needUpdate = true;
            }
            if (needUpdate) {
                teamMemberMapper.updateById(m);
            }
        }
    }
}


