/**
 * 配置审计服务接口：系统配置变更审计服务
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
package com.okbug.platform.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.dto.system.ConfigChangeLogDTO;
import com.okbug.platform.dto.system.ConfigChangeLogQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * 配置审计服务接口
 * 定义配置变更审计的所有业务方法
 * 包括变更记录、查询统计、影响分析等核心功能
 */
public interface ConfigAuditService {
    
    /**
     * 记录配置创建操作
     * 
     * @param configId 配置ID
     * @param configKey 配置键
     * @param newValue 新配置值
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param ipAddress 操作IP地址
     */
    void logConfigCreate(Long configId, String configKey, String newValue, 
                        Long operatorId, String operatorName, String ipAddress);
    
    /**
     * 记录配置更新操作
     * 
     * @param configId 配置ID
     * @param configKey 配置键
     * @param oldValue 旧配置值
     * @param newValue 新配置值
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param ipAddress 操作IP地址
     */
    void logConfigUpdate(Long configId, String configKey, String oldValue, String newValue,
                        Long operatorId, String operatorName, String ipAddress);
    
    /**
     * 记录配置删除操作
     * 
     * @param configId 配置ID
     * @param configKey 配置键
     * @param oldValue 删除前的配置值
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param ipAddress 操作IP地址
     */
    void logConfigDelete(Long configId, String configKey, String oldValue,
                        Long operatorId, String operatorName, String ipAddress);
    
    /**
     * 分页查询配置变更日志
     * 
     * 查询功能：
     * 1. 支持按配置键、操作类型、操作人等条件筛选
     * 2. 支持按时间范围筛选
     * 3. 提供分页查询能力
     * 4. 结果按操作时间倒序排列
     * 
     * @param queryDto 查询条件DTO
     * @return 分页结果，包含变更日志列表和分页信息
     */
    IPage<ConfigChangeLogDTO> getChangeLogs(ConfigChangeLogQueryDTO queryDto);
    
    /**
     * 根据配置ID获取变更历史
     * 
     * @param configId 配置ID
     * @return 配置变更历史列表
     */
    List<ConfigChangeLogDTO> getChangeHistoryByConfigId(Long configId);
    
    /**
     * 根据配置键获取变更历史
     * 
     * @param configKey 配置键
     * @return 配置变更历史列表
     */
    List<ConfigChangeLogDTO> getChangeHistoryByConfigKey(String configKey);
    
    
    
    /**
     * 获取配置变更影响分析
     * 
     * 分析内容：
     * 1. 配置变更对系统功能的影响
     * 2. 配置变更对用户体验的影响
     * 3. 配置变更的风险评估
     * 
     * @param configKey 配置键
     * @param changeType 变更类型
     * @return 影响分析结果
     */
    Map<String, Object> getChangeImpactAnalysis(String configKey, String changeType);
    
    /**
     * 获取配置回滚建议
     * 
     * 建议内容：
     * 1. 基于变更历史的回滚建议
     * 2. 基于影响分析的优先级建议
     * 3. 基于时间窗口的回滚策略建议
     * 
     * @param configKey 配置键
     * @return 回滚建议
     */
    Map<String, Object> getRollbackRecommendations(String configKey);
    
    /**
     * 清理过期的变更日志
     * 
     * 清理逻辑：
     * 1. 根据配置的保留期限清理过期日志
     * 2. 保留重要的审计信息
     * 3. 支持手动清理和自动清理
     * 
     * @param retentionDays 保留天数
     * @return 清理的记录数量
     */
    Integer cleanExpiredLogs(Integer retentionDays);
    
    /**
     * 导出配置变更日志
     * 
     * @param queryDto 查询条件
     * @param format 导出格式（CSV、EXCEL、PDF）
     * @return 导出文件路径
     */
    String exportChangeLogs(ConfigChangeLogQueryDTO queryDto, String format);
} 