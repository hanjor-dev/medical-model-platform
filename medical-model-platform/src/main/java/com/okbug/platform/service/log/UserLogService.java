/**
 * 用户日志服务接口
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:30:00
 */
package com.okbug.platform.service.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.dto.log.LogQueryRequest;
import com.okbug.platform.dto.log.LogResponse;
import com.okbug.platform.entity.auth.OperationLog;

import java.time.LocalDateTime;

public interface UserLogService {
    
    /**
     * 分页查询用户日志（统一登录日志和操作日志）
     */
    IPage<LogResponse> queryLogs(LogQueryRequest request);
    
    /**
     * 获取日志详情
     */
    LogResponse getLogDetail(Long logId);
    
    
    
    /**
     * 记录用户操作日志
     */
    void recordOperationLog(OperationLog operationLog);
    
    /**
     * 异步记录操作日志（推荐使用，不影响主业务性能）
     */
    void recordOperationLogAsync(OperationLog operationLog);
    
    /**
     * 批量删除过期日志
     */
    int cleanExpiredLogs(LocalDateTime expireTime);
    
    /**
     * 导出日志数据
     */
    byte[] exportLogs(LogQueryRequest request, String exportType);
    
    /**
     * 获取操作模块选项
     */
    java.util.List<java.util.Map<String, String>> getOperationModuleOptions();
    
    // 操作类型选项移除
} 
