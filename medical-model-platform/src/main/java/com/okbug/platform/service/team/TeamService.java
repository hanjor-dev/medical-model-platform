package com.okbug.platform.service.team;

import com.okbug.platform.entity.team.Team;
import com.okbug.platform.vo.team.TeamVO;

/**
 * 团队服务：封装团队核心业务与写操作，供 Controller 调用
 */
public interface TeamService {

    Team findByTeamCode(String teamCode);

    Team getById(Long teamId);

    /** 创建团队并返回实体（包含 owner 设定由调用方处理或内部处理） */
    Team createTeam(Long ownerUserId, String teamName, String description);

    /** 更新团队名称/描述/状态（状态允许 SUPER_ADMIN 或管理者切换） */
    void updateTeam(Long teamId, String teamName, String description, Integer status);

    /** 软删除团队 */
    void softDeleteTeam(Long teamId);

    /** 添加成员（若已存在启用成员则报错） */
    void addMember(Long teamId, Long userId, String role);

    /** 更新成员角色/状态（不存在则报错） */
    void updateMember(Long teamId, Long userId, String role, Integer status);

    /** 移除成员（不存在视为无操作） */
    void removeMember(Long teamId, Long userId);

    /**
     * 转移团队拥有者：由当前OWNER将拥有者转移给目标用户
     * 要求：
     * - 目标用户必须是团队启用成员
     * - 转移后目标用户为OWNER，原OWNER自动降为ADMIN
     */
    void transferOwner(Long teamId, Long fromOwnerUserId, Long toUserId);

    /**
     * 安全的分页查询团队列表（根据操作者权限返回范围）。
     */
    com.baomidou.mybatisplus.core.metadata.IPage<TeamVO> listTeams(Long operatorUserId, Integer pageNum, Integer pageSize, String keyword);

    /**
     * 安全读取团队详情（成员可见）。
     */
    TeamVO getTeamDetail(Long operatorUserId, Long teamId);

    /**
     * 安全更新团队信息（需管理权限）。
     */
    void updateTeamSecure(Long operatorUserId, Long teamId, String teamName, String description, Integer status);

    /**
     * 安全软删除团队（需管理权限）。
     */
    void softDeleteTeamSecure(Long operatorUserId, Long teamId);

    /**
     * 通过团队码预览（含启用状态校验）。
     */
    TeamVO previewByCode(String teamCode);

    /**
     * 安全分页查询成员列表。
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.okbug.platform.vo.team.TeamMemberVO> listMembers(Long operatorUserId, Long teamId, Integer pageNum, Integer pageSize, String role, Integer status);

    /**
     * 安全添加成员（需管理权限）。
     */
    void addMemberSecure(Long operatorUserId, Long teamId, Long userId, String role);

    /**
     * 安全更新成员（需管理权限）。
     */
    void updateMemberSecure(Long operatorUserId, Long teamId, Long userId, String role, Integer status);

    /**
     * 安全移除成员（需管理权限）。
     */
    void removeMemberSecure(Long operatorUserId, Long teamId, Long userId);

    /**
     * 安全转移拥有者（只有当前OWNER可执行）。
     */
    void transferOwnerSecure(Long operatorUserId, Long teamId, Long toUserId);

    /**
     * 当前登录用户退出团队（仅限本人，且不允许OWNER退出）。
     */
    void exitTeamSelf(Long operatorUserId, Long teamId);

    /**
     * 解散团队（仅OWNER）：软删除团队并禁用/逻辑删除所有成员关系。
     */
    void dissolveTeamSecure(Long operatorUserId, Long teamId);

    /**
     * 仅供 SUPER_ADMIN 使用：直接设置团队状态（不受团队禁用限制）。
     */
    void setTeamStatusBySuperAdmin(Long operatorUserId, Long teamId, Integer status);
}


