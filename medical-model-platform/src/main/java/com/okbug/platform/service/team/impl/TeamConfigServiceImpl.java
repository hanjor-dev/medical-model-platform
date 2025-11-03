/**
 * 团队配置服务实现
 *
 * 职责：团队级 KV 配置的读取与写入；含权限校验、缺省值策略与幂等更新。
 * 约定：读取走 DEBUG 日志，变更操作记录 INFO 日志。
 */
package com.okbug.platform.service.team.impl;

import com.okbug.platform.mapper.team.TeamConfigMapper;
import com.okbug.platform.service.team.TeamConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.team.TeamConfig;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.team.UpdateTeamConfigRequest;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamConfigServiceImpl implements TeamConfigService {

    private final TeamConfigMapper teamConfigMapper;
    private final TeamAccessService teamAccessService;

    private static final String KEY_JOIN_REQUIRE_APPROVAL = "team.join.requireApproval";

    /**
     * 判断团队是否开启“加入审批”开关。
     *
     * @param teamId 团队ID
     * @return true 表示需要审批；false 表示无需审批
     */
    @Override
    public boolean isJoinApprovalRequired(Long teamId) {
        if (teamId == null) {
            return false;
        }
        LambdaQueryWrapper<TeamConfig> query = new LambdaQueryWrapper<>();
        query.eq(TeamConfig::getTeamId, teamId)
             .eq(TeamConfig::getConfigKey, KEY_JOIN_REQUIRE_APPROVAL);
        TeamConfig config = teamConfigMapper.selectOne(query);
        if (config == null || config.getConfigValue() == null) {
            log.debug("isJoinApprovalRequired: teamId={}, value=default(false)", teamId);
            return false;
        }
        String v = config.getConfigValue().trim().toLowerCase();
        boolean enabled = "true".equals(v) || "1".equals(v) || "yes".equals(v);
        log.debug("isJoinApprovalRequired: teamId={}, value={}", teamId, enabled);
        return enabled;
    }

    /**
     * 获取团队配置（带权限校验）。
     *
     * @param operatorUserId 操作者
     * @param teamId 团队ID
     * @return 配置键值对
     * @throws ServiceException FORBIDDEN 无权访问
     */
    @Override
    public Map<String, String> getConfigs(Long operatorUserId, Long teamId) {
        if (!teamAccessService.canManageTeam(operatorUserId, teamId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权查看团队配置");
        }
        LambdaQueryWrapper<TeamConfig> q = new LambdaQueryWrapper<>();
        q.eq(TeamConfig::getTeamId, teamId);
        List<TeamConfig> list = teamConfigMapper.selectList(q);
        Map<String, String> configs = new HashMap<>();
        if (list != null) {
            for (TeamConfig c : list) {
                configs.put(c.getConfigKey(), c.getConfigValue());
            }
        }
        log.debug("team configs loaded: operator={}, teamId={}, size={}", operatorUserId, teamId, configs.size());
        return configs;
    }

    /**
     * 覆盖更新团队配置（逐键 upsert），带权限校验。
     *
     * @param operatorUserId 操作者
     * @param request 更新请求体
     * @throws ServiceException FORBIDDEN 无权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigs(Long operatorUserId, UpdateTeamConfigRequest request) {
        if (!teamAccessService.canManageTeam(operatorUserId, request.getTeamId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权更新团队配置");
        }
        Map<String, String> map = request.getConfigs();
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : map.entrySet()) {
            LambdaQueryWrapper<TeamConfig> q = new LambdaQueryWrapper<>();
            q.eq(TeamConfig::getTeamId, request.getTeamId())
             .eq(TeamConfig::getConfigKey, e.getKey())
             .last("limit 1");
            TeamConfig existing = teamConfigMapper.selectOne(q);
            if (existing == null) {
                TeamConfig c = new TeamConfig();
                c.setTeamId(request.getTeamId());
                c.setConfigKey(e.getKey());
                c.setConfigValue(e.getValue());
                teamConfigMapper.insert(c);
            } else {
                existing.setConfigValue(e.getValue());
                teamConfigMapper.updateById(existing);
            }
        }
        log.info("team configs updated: operator={}, teamId={}, keys={}",
                operatorUserId, request.getTeamId(), map.size());
    }

    @Override
    /**
     * 读取单个配置值。
     *
     * @param teamId 团队ID
     * @param configKey 配置键
     * @return 配置值；当参数非法或未配置时返回 null
     */
    public String getConfigValue(Long teamId, String configKey) {
        if (teamId == null || configKey == null || configKey.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<TeamConfig> q = new LambdaQueryWrapper<>();
        q.eq(TeamConfig::getTeamId, teamId)
         .eq(TeamConfig::getConfigKey, configKey)
         .last("limit 1");
        TeamConfig c = teamConfigMapper.selectOne(q);
        return c == null ? null : c.getConfigValue();
    }

    
}


