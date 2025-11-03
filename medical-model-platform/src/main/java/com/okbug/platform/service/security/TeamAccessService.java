package com.okbug.platform.service.security;

import java.util.List;

/**
 * 团队访问边界校验服务：统一判定团队域范围与管理权限
 *
 * 约定：
 * - SUPER_ADMIN 全局放行
 * - 团队 OWNER/ADMIN 可管理同团队成员与团队资源
 * - 普通用户仅能管理自己
 */
public interface TeamAccessService {

    /**
     * 操作人是否可以管理目标用户
     */
    boolean canManageUser(Long operatorUserId, Long targetUserId);

    /**
     * 操作人是否可以管理目标团队
     */
    boolean canManageTeam(Long operatorUserId, Long teamId);

    /**
     * 操作人是否可以查看团队管理数据（成员列表、加入申请等），不受团队禁用影响。
     * 语义：SUPER_ADMIN 或 团队 OWNER/ADMIN 即可查看。
     */
    boolean canViewTeamForManagement(Long operatorUserId, Long teamId);

    /**
     * 两个用户是否属于同一个有效团队
     */
    boolean isSameTeam(Long userIdA, Long userIdB);

    /**
     * 返回操作人可管理的用户ID集合（包含自己）。
     * 注意：集合可能较大，调用方需谨慎用于 IN 过滤。
     */
    List<Long> listManagedUserIds(Long operatorUserId);
}


