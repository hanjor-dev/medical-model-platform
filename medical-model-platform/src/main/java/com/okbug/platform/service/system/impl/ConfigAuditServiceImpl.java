/**
 * 配置审计服务实现类：系统配置变更审计服务实现
 * 
 * 核心功能：
 * 1. 记录配置项的创建、修改、删除操作
 * 2. 保存配置变更前后的值，支持回滚操作
 * 3. 记录操作人信息和操作时间
 * 4. 提供配置变更查询和统计功能
 * 5. 支持配置变更影响分析
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.utils.IpUtils;
import com.okbug.platform.dto.system.ConfigChangeLogDTO;
import com.okbug.platform.dto.system.ConfigChangeLogQueryDTO;
import com.okbug.platform.entity.system.ConfigChangeLog;
import com.okbug.platform.mapper.system.ConfigChangeLogMapper;
import com.okbug.platform.service.system.ConfigAuditService;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.entity.auth.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置审计服务实现类
 * 实现ConfigAuditService接口定义的所有审计方法
 * 提供完整的配置变更追踪和分析功能
 */
@Service
@Slf4j
public class ConfigAuditServiceImpl implements ConfigAuditService {
    
    @Autowired
    private ConfigChangeLogMapper configChangeLogMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public void logConfigCreate(Long configId, String configKey, String newValue, 
                               Long operatorId, String operatorName, String ipAddress) {
        log.info("记录配置创建操作，配置ID: {}, 配置键: {}, 操作人: {}", configId, configKey, operatorName);
        
        OperatorContext ctx = resolveOperatorContext(operatorId, operatorName, ipAddress);
        
        ConfigChangeLog changeLog = new ConfigChangeLog();
        changeLog.setConfigId(configId);
        changeLog.setConfigKey(configKey);
        // 避免DB非空约束问题：CREATE时oldValue设为空字符串
        changeLog.setOldValue("");
        changeLog.setNewValue(newValue == null ? "" : newValue);
        changeLog.setChangeType("CREATE");
        changeLog.setOperatorId(ctx.id);
        changeLog.setOperatorName(ctx.name);
        changeLog.setOperationTime(LocalDateTime.now());
        changeLog.setIpAddress(ctx.ip);
        
        configChangeLogMapper.insert(changeLog);
        log.debug("配置创建操作记录完成，日志ID: {}", changeLog.getId());
    }
    
    @Override
    public void logConfigUpdate(Long configId, String configKey, String oldValue, String newValue,
                               Long operatorId, String operatorName, String ipAddress) {
        log.info("记录配置更新操作，配置ID: {}, 配置键: {}, 操作人: {}", configId, configKey, operatorName);
        
        OperatorContext ctx = resolveOperatorContext(operatorId, operatorName, ipAddress);
        
        ConfigChangeLog changeLog = new ConfigChangeLog();
        changeLog.setConfigId(configId);
        changeLog.setConfigKey(configKey);
        // 避免DB非空约束问题：将可能的null值标准化为空字符串
        changeLog.setOldValue(oldValue == null ? "" : oldValue);
        changeLog.setNewValue(newValue == null ? "" : newValue);
        changeLog.setChangeType("UPDATE");
        changeLog.setOperatorId(ctx.id);
        changeLog.setOperatorName(ctx.name);
        changeLog.setOperationTime(LocalDateTime.now());
        changeLog.setIpAddress(ctx.ip);
        
        configChangeLogMapper.insert(changeLog);
        log.debug("配置更新操作记录完成，日志ID: {}", changeLog.getId());
    }
    
    @Override
    public void logConfigDelete(Long configId, String configKey, String oldValue,
                               Long operatorId, String operatorName, String ipAddress) {
        log.info("记录配置删除操作，配置ID: {}, 配置键: {}, 操作人: {}", configId, configKey, operatorName);
        
        OperatorContext ctx = resolveOperatorContext(operatorId, operatorName, ipAddress);
        
        ConfigChangeLog changeLog = new ConfigChangeLog();
        changeLog.setConfigId(configId);
        changeLog.setConfigKey(configKey);
        // 避免DB非空约束问题：DELETE时newValue设为空字符串
        changeLog.setOldValue(oldValue == null ? "" : oldValue);
        changeLog.setNewValue("");
        changeLog.setChangeType("DELETE");
        changeLog.setOperatorId(ctx.id);
        changeLog.setOperatorName(ctx.name);
        changeLog.setOperationTime(LocalDateTime.now());
        changeLog.setIpAddress(ctx.ip);
        
        configChangeLogMapper.insert(changeLog);
        log.debug("配置删除操作记录完成，日志ID: {}", changeLog.getId());
    }
    
    @Override
    public IPage<ConfigChangeLogDTO> getChangeLogs(ConfigChangeLogQueryDTO queryDto) {
        log.debug("开始查询配置变更日志，查询条件: {}", queryDto);
        
        // 构建查询条件
        LambdaQueryWrapper<ConfigChangeLog> query = new LambdaQueryWrapper<>();
        
        // 配置键筛选
        if (StringUtils.hasText(queryDto.getConfigKey())) {
            query.like(ConfigChangeLog::getConfigKey, queryDto.getConfigKey());
        }
        
        // 变更类型筛选
        if (StringUtils.hasText(queryDto.getChangeType())) {
            query.eq(ConfigChangeLog::getChangeType, queryDto.getChangeType());
        }
        
        // 操作人ID筛选
        if (queryDto.getOperatorId() != null) {
            query.eq(ConfigChangeLog::getOperatorId, queryDto.getOperatorId());
        }
        
        // 操作人姓名筛选
        if (StringUtils.hasText(queryDto.getOperatorName())) {
            query.like(ConfigChangeLog::getOperatorName, queryDto.getOperatorName());
        }
        
        // 时间范围筛选
        if (queryDto.getStartTime() != null) {
            query.ge(ConfigChangeLog::getOperationTime, queryDto.getStartTime());
        }
        if (queryDto.getEndTime() != null) {
            query.le(ConfigChangeLog::getOperationTime, queryDto.getEndTime());
        }
        
        // 按操作时间倒序排列
        query.orderByDesc(ConfigChangeLog::getOperationTime);
        
        // 分页查询
        Page<ConfigChangeLog> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        IPage<ConfigChangeLog> result = configChangeLogMapper.selectPage(page, query);
        
        // 转换为DTO
        List<ConfigChangeLogDTO> logs = result.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 构建返回结果
        Page<ConfigChangeLogDTO> dtoPage = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        dtoPage.setRecords(logs);
        dtoPage.setTotal(result.getTotal());
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        
        log.debug("配置变更日志查询完成，共查询到 {} 条记录", result.getTotal());
        return dtoPage;
    }
    
    @Override
    public List<ConfigChangeLogDTO> getChangeHistoryByConfigId(Long configId) {
        log.debug("根据配置ID获取变更历史，配置ID: {}", configId);
        
        LambdaQueryWrapper<ConfigChangeLog> query = new LambdaQueryWrapper<>();
        query.eq(ConfigChangeLog::getConfigId, configId)
             .orderByDesc(ConfigChangeLog::getOperationTime);
        
        List<ConfigChangeLog> logs = configChangeLogMapper.selectList(query);
        List<ConfigChangeLogDTO> logDtos = logs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        log.debug("配置变更历史获取完成，配置ID: {}, 记录数量: {}", configId, logDtos.size());
        return logDtos;
    }
    
    @Override
    public List<ConfigChangeLogDTO> getChangeHistoryByConfigKey(String configKey) {
        log.debug("根据配置键获取变更历史，配置键: {}", configKey);
        
        LambdaQueryWrapper<ConfigChangeLog> query = new LambdaQueryWrapper<>();
        query.eq(ConfigChangeLog::getConfigKey, configKey)
             .orderByDesc(ConfigChangeLog::getOperationTime);
        
        List<ConfigChangeLog> logs = configChangeLogMapper.selectList(query);
        List<ConfigChangeLogDTO> logDtos = logs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        log.debug("配置变更历史获取完成，配置键: {}, 记录数量: {}", configKey, logDtos.size());
        return logDtos;
    }
    
    
    
    @Override
    public Map<String, Object> getChangeImpactAnalysis(String configKey, String changeType) {
        log.debug("开始获取配置变更影响分析，配置键: {}, 变更类型: {}", configKey, changeType);
        
        Map<String, Object> impactAnalysis = new HashMap<>();
        
        // 获取配置变更历史
        List<ConfigChangeLogDTO> changeHistory = getChangeHistoryByConfigKey(configKey);
        
        // 分析变更频率
        long changeFrequency = changeHistory.size();
        impactAnalysis.put("changeFrequency", changeFrequency);
        
        // 分析变更类型分布
        Map<String, Long> changeTypeDistribution = changeHistory.stream()
            .collect(Collectors.groupingBy(ConfigChangeLogDTO::getChangeType, Collectors.counting()));
        impactAnalysis.put("changeTypeDistribution", changeTypeDistribution);
        
        // 分析操作人分布
        Map<String, Long> operatorDistribution = changeHistory.stream()
            .collect(Collectors.groupingBy(ConfigChangeLogDTO::getOperatorName, Collectors.counting()));
        impactAnalysis.put("operatorDistribution", operatorDistribution);
        
        // 风险评估
        String riskLevel = assessRiskLevel(changeFrequency, changeType);
        impactAnalysis.put("riskLevel", riskLevel);
        
        // 影响建议
        List<String> recommendations = generateRecommendations(changeFrequency, changeType, riskLevel);
        impactAnalysis.put("recommendations", recommendations);
        
        log.debug("配置变更影响分析完成，风险等级: {}", riskLevel);
        return impactAnalysis;
    }
    
    @Override
    public Map<String, Object> getRollbackRecommendations(String configKey) {
        log.debug("开始获取配置回滚建议，配置键: {}", configKey);
        
        Map<String, Object> recommendations = new HashMap<>();
        
        // 获取配置变更历史
        List<ConfigChangeLogDTO> changeHistory = getChangeHistoryByConfigKey(configKey);
        
        if (changeHistory.isEmpty()) {
            recommendations.put("message", "该配置项没有变更历史");
            return recommendations;
        }
        
        // 找到最近的更新操作
        Optional<ConfigChangeLogDTO> lastUpdate = changeHistory.stream()
            .filter(log -> "UPDATE".equals(log.getChangeType()))
            .findFirst();
        
        if (lastUpdate.isPresent()) {
            ConfigChangeLogDTO lastUpdateLog = lastUpdate.get();
            recommendations.put("lastUpdateTime", lastUpdateLog.getOperationTime());
            recommendations.put("lastUpdateOperator", lastUpdateLog.getOperatorName());
            recommendations.put("oldValue", lastUpdateLog.getOldValue());
            recommendations.put("newValue", lastUpdateLog.getNewValue());
            recommendations.put("canRollback", true);
            recommendations.put("rollbackValue", lastUpdateLog.getOldValue());
        } else {
            recommendations.put("canRollback", false);
            recommendations.put("message", "没有找到可回滚的更新操作");
        }
        
        // 回滚策略建议
        List<String> rollbackStrategies = new ArrayList<>();
        rollbackStrategies.add("立即回滚：如果配置变更导致系统异常，建议立即回滚到上一个稳定版本");
        rollbackStrategies.add("渐进回滚：如果影响范围较大，建议分步骤回滚，观察系统状态");
        rollbackStrategies.add("备份回滚：回滚前建议备份当前配置，以便需要时恢复");
        recommendations.put("rollbackStrategies", rollbackStrategies);
        
        log.debug("配置回滚建议获取完成，可回滚: {}", recommendations.get("canRollback"));
        return recommendations;
    }
    
    @Override
    public Integer cleanExpiredLogs(Integer retentionDays) {
        log.info("开始清理过期的配置变更日志，保留天数: {}", retentionDays);
        
        if (retentionDays == null || retentionDays <= 0) {
            retentionDays = 90; // 默认保留90天
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        
        LambdaQueryWrapper<ConfigChangeLog> query = new LambdaQueryWrapper<>();
        query.lt(ConfigChangeLog::getOperationTime, cutoffTime);
        
        int deleteCount = configChangeLogMapper.delete(query);
        
        log.info("过期配置变更日志清理完成，删除记录数: {}", deleteCount);
        return deleteCount;
    }
    
    @Override
    public String exportChangeLogs(ConfigChangeLogQueryDTO queryDto, String format) {
        log.info("开始导出配置变更日志，格式: {}", format);
        
        // 这里应该实现具体的导出逻辑
        // 支持CSV、EXCEL、PDF等格式
        // 实际项目中应该使用专门的导出库
        
        String exportPath = "/tmp/config_change_logs_" + System.currentTimeMillis() + "." + format.toLowerCase();
        
        log.info("配置变更日志导出完成，文件路径: {}", exportPath);
        return exportPath;
    }
    
    /**
     * 解析并补全操作人上下文信息
     * 优先从Sa-Token上下文获取，兼容传入参数作为兜底
     */
    private OperatorContext resolveOperatorContext(Long operatorId, String operatorName, String ipAddress) {
        Long resolvedId = operatorId;
        String resolvedName = operatorName;
        String resolvedIp = ipAddress;
        
        try {
            if (StpUtil.isLogin()) {
                resolvedId = StpUtil.getLoginIdAsLong();
                // 直接查询数据库获取昵称或用户名，避免服务层循环依赖
                try {
                    User currentUser = userMapper.selectById(resolvedId);
                    if (currentUser != null) {
                        resolvedName = currentUser.getNickname() != null && currentUser.getNickname().trim().length() > 0
                            ? currentUser.getNickname()
                            : currentUser.getUsername();
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
            // 忽略从StpUtil读取失败
        }
        
        // 获取请求IP
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest request = attrs.getRequest();
                String ip = IpUtils.getClientIp(request);
                if (ip != null && ip.trim().length() > 0) {
                    resolvedIp = ip;
                }
            }
        } catch (Exception ignored) {
        }
        
        if (resolvedId == null) {
            resolvedId = 0L; // 后端兜底，避免DB非空约束
        }
        if (resolvedName == null || resolvedName.trim().isEmpty()) {
            resolvedName = resolvedId != null && resolvedId > 0 ? ("USER-" + resolvedId) : "SYSTEM";
        }
        
        if (resolvedIp == null || resolvedIp.trim().isEmpty()) {
            resolvedIp = "127.0.0.1";
        }
        
        OperatorContext ctx = new OperatorContext();
        ctx.id = resolvedId;
        ctx.name = resolvedName;
        ctx.ip = resolvedIp;
        return ctx;
    }
    
    /**
     * 操作人上下文信息
     */
    private static class OperatorContext {
        Long id;
        String name;
        String ip;
    }
    
    /**
     * 计算开始时间
     * 
     * @param timeRange 时间范围
     * @return 开始时间
     */
    // 统计相关功能移除
    
    /**
     * 评估风险等级
     * 
     * @param changeFrequency 变更频率
     * @param changeType 变更类型
     * @return 风险等级
     */
    private String assessRiskLevel(long changeFrequency, String changeType) {
        if (changeFrequency > 100) {
            return "HIGH"; // 高风险
        } else if (changeFrequency > 50) {
            return "MEDIUM"; // 中风险
        } else if (changeFrequency > 10) {
            return "LOW"; // 低风险
        } else {
            return "MINIMAL"; // 最小风险
        }
    }
    
    /**
     * 生成建议
     * 
     * @param changeFrequency 变更频率
     * @param changeType 变更类型
     * @param riskLevel 风险等级
     * @return 建议列表
     */
    private List<String> generateRecommendations(long changeFrequency, String changeType, String riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        if ("HIGH".equals(riskLevel)) {
            recommendations.add("该配置项变更频繁，建议加强变更管理流程");
            recommendations.add("考虑实施变更审批机制，减少不必要的配置变更");
            recommendations.add("建议定期审查配置变更历史，识别潜在问题");
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendations.add("该配置项变更适中，建议保持当前的变更管理流程");
            recommendations.add("定期检查配置变更的影响范围");
        } else {
            recommendations.add("该配置项变更较少，风险较低");
            recommendations.add("继续保持当前的配置管理策略");
        }
        
        return recommendations;
    }
    
    /**
     * 将ConfigChangeLog实体转换为ConfigChangeLogDTO
     * 
     * @param log 配置变更日志实体
     * @return 配置变更日志DTO
     */
    private ConfigChangeLogDTO convertToDTO(ConfigChangeLog log) {
        return ConfigChangeLogDTO.builder()
            .id(log.getId())
            .configId(log.getConfigId())
            .configKey(log.getConfigKey())
            .oldValue(log.getOldValue())
            .newValue(log.getNewValue())
            .changeType(log.getChangeType())
            .operatorId(log.getOperatorId())
            .operatorName(log.getOperatorName())
            .operationTime(log.getOperationTime())
            .ipAddress(log.getIpAddress())
            .build();
    }
}
