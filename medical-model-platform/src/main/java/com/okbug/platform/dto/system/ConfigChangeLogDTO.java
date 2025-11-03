/**
 * 配置变更日志DTO：配置变更日志数据传输对象
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeLogDTO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 配置ID
     */
    private Long configId;
    
    /**
     * 配置键
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
     */
    private String changeType;
    
    /**
     * 操作人ID
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
