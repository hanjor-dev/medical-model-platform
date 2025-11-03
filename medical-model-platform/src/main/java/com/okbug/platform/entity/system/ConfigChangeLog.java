/**
 * 配置变更日志实体类：记录系统配置的变更历史
 * 
 * 核心功能：
 * 1. 记录配置项的创建、修改、删除操作
 * 2. 保存配置变更前后的值，支持回滚操作
 * 3. 记录操作人信息和操作时间
 * 4. 提供完整的配置变更审计追踪
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_change_logs")
public class ConfigChangeLog {
    
    /**
     * 日志ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置ID，关联system_configs表
     */
    private Long configId;
    
    /**
     * 配置键，如：user.login.fail.max.count
     */
    private String configKey;
    
    /**
     * 修改前的配置值
     */
    private String oldValue;
    
    /**
     * 修改后的配置值
     */
    private String newValue;
    
    /**
     * 变更类型
     * CREATE: 创建
     * UPDATE: 修改
     * DELETE: 删除
     */
    private String changeType;
    
    /**
     * 操作人ID，关联users表
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 操作IP地址
     */
    private String ipAddress;
} 