/**
 * 团队成员服务接口：定义团队成员相关的基础业务操作（占位）
 */
package com.okbug.platform.service.team;

public interface TeamMemberService {
    /**
     * 判断用户是否已是团队成员
     */
    boolean isMember(Long teamId, Long userId);

    /**
     * 将用户加入团队为普通成员（已存在则忽略）
     */
    void addMemberIfAbsent(Long teamId, Long userId);

    /**
     * 获取用户的一个有效成员关系（若存在多个，返回任意一个）
     */
    com.okbug.platform.entity.team.TeamMember getFirstActiveMembership(Long userId);
}


