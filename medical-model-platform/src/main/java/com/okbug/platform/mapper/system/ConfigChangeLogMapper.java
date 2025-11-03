/**
 * 配置变更日志Mapper接口：配置变更日志数据访问层
 * 
 * 核心功能：
 * 1. 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 2. 支持配置变更日志的查询和统计
 * 3. 提供按时间范围、操作人等条件的查询方法
 * 4. 支持日志清理和导出功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.system.ConfigChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配置变更日志数据访问接口
 * 继承BaseMapper获得基础的CRUD操作能力
 */
@Mapper
public interface ConfigChangeLogMapper extends BaseMapper<ConfigChangeLog> {
    
    // 继承BaseMapper后，已经具备了基础的CRUD操作能力
    // 包括：insert、delete、update、select等操作
    // 如果需要特殊的查询逻辑，可以在这里添加自定义方法
    
} 