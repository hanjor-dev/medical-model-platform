/**
 * 团队配置服务接口：定义团队配置相关的基础业务操作（占位）
 */
package com.okbug.platform.service.team;

public interface TeamConfigService {
    /**
     * 是否开启加入审批
     */
    boolean isJoinApprovalRequired(Long teamId);

    /**
     * 获取团队配置（全部键值对）。包含权限校验与业务异常抛出。
     *
     * @param operatorUserId 操作人用户ID
     * @param teamId 团队ID
     * @return 配置键值对
     */
    java.util.Map<String, String> getConfigs(Long operatorUserId, Long teamId);

    /**
     * 更新团队配置（覆盖写入）。包含权限校验与业务异常抛出。
     *
     * @param operatorUserId 操作人用户ID
     * @param request 更新请求
     */
    void updateConfigs(Long operatorUserId, com.okbug.platform.dto.team.UpdateTeamConfigRequest request);

    /**
     * 获取团队配置值（无权限校验，供内部服务调用）。
     * 不存在则返回 null。
     */
    String getConfigValue(Long teamId, String configKey);

    
}


