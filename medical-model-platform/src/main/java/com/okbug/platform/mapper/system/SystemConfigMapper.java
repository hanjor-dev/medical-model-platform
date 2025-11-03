/**
 * 系统配置Mapper接口：系统配置数据访问层
 * 
 * 核心功能：
 * 1. 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 2. 使用Java API实现自定义查询方法，无需XML映射文件
 * 3. 提供配置键查询、配置键计数等业务方法
 * 4. 支持逻辑删除和软删除操作
 * 5. 支持批量更新状态和排序操作
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.okbug.platform.entity.system.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统配置数据访问接口
 * 继承BaseMapper获得基础的CRUD操作能力
 * 使用default方法实现自定义查询逻辑，避免XML映射文件
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置
     * 
     * 查询逻辑：
     * 1. 根据配置键精确匹配
     * 2. 只查询未删除的配置（isDeleted = 0）
     * 3. 返回单个配置对象，如果不存在则返回null
     * 
     * @param configKey 配置键，如：user.login.fail.max.count
     * @return 系统配置对象，如果不存在则返回null
     */
    default SystemConfig selectByConfigKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.eq(SystemConfig::getConfigKey, configKey)
             .eq(SystemConfig::getIsDeleted, 0);
        return selectOne(query);
    }
    
    /**
     * 检查配置键是否存在
     * 
     * 检查逻辑：
     * 1. 根据配置键精确匹配
     * 2. 只统计未删除的配置（isDeleted = 0）
     * 3. 返回匹配的记录数量
     * 
     * @param configKey 配置键，如：user.login.fail.max.count
     * @return 匹配的记录数量，0表示不存在，大于0表示已存在
     */
    default Long countByConfigKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.eq(SystemConfig::getConfigKey, configKey)
             .eq(SystemConfig::getIsDeleted, 0);
        return selectCount(query);
    }
    
    /**
     * 更新配置值
     * 
     * 更新逻辑：
     * 1. 根据配置键精确匹配
     * 2. 只更新未删除的配置（isDeleted = 0）
     * 3. 更新配置值和更新时间
     * 4. 返回更新的记录数量
     * 
     * @param configKey 配置键，如：user.login.fail.max.count
     * @param configValue 新的配置值
     * @return 更新的记录数量，0表示未找到匹配记录，1表示更新成功
     */
    default int updateConfigValue(String configKey, String configValue) {
        LambdaUpdateWrapper<SystemConfig> update = new LambdaUpdateWrapper<>();
        update.eq(SystemConfig::getConfigKey, configKey)
              .eq(SystemConfig::getIsDeleted, 0)
              .set(SystemConfig::getConfigValue, configValue)
              .set(SystemConfig::getUpdateTime, java.time.LocalDateTime.now());
        return update(null, update);
    }
    
    /**
     * 批量更新配置状态
     * 
     * 更新逻辑：
     * 1. 根据ID列表批量更新状态
     * 2. 同时更新updateTime字段
     * 3. 返回更新成功的记录数量
     * 
     * @param ids 配置ID列表
     * @param status 目标状态，0:禁用，1:启用
     * @return 更新成功的记录数量
     */
    default Integer batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SystemConfig::getId, ids)
                    .set(SystemConfig::getStatus, status)
                    .set(SystemConfig::getUpdateTime, java.time.LocalDateTime.now());
        
        return update(null, updateWrapper);
    }
    
    /**
     * 批量更新配置排序
     * 
     * 更新逻辑：
     * 1. 根据ID和排序值的映射关系批量更新
     * 2. 同时更新updateTime字段
     * 3. 返回更新成功的记录数量
     * 
     * @param idSortMap 配置ID和排序值的映射关系
     * @return 更新成功的记录数量
     */
    default Integer batchUpdateSortOrder(@Param("idSortMap") Map<Long, Integer> idSortMap) {
        if (idSortMap == null || idSortMap.isEmpty()) {
            return 0;
        }
        
        int updateCount = 0;
        for (Map.Entry<Long, Integer> entry : idSortMap.entrySet()) {
            Long id = entry.getKey();
            Integer sortOrder = entry.getValue();
            
            LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SystemConfig::getSortOrder, sortOrder)
                        .set(SystemConfig::getUpdateTime, java.time.LocalDateTime.now())
                        .eq(SystemConfig::getId, id);
            
            if (update(null, updateWrapper) > 0) {
                updateCount++;
            }
        }
        
        return updateCount;
    }
} 