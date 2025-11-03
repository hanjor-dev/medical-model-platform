/**
 * 系统字典Mapper接口：系统字典数据访问层
 * 
 * 核心功能：
 * 1. 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 2. 使用Java API实现自定义查询方法，无需XML映射文件
 * 3. 提供字典编码查询、父子关系查询等业务方法
 * 4. 支持逻辑删除和软删除操作
 * 5. 使用path作为dictCode，确保唯一性
 * 6. 支持批量更新状态和排序操作
 * 
 * @author hanjor
 * @version 2.1
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.okbug.platform.entity.system.SystemDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 系统字典数据访问接口
 * 继承BaseMapper获得基础的CRUD操作能力
 * 使用default方法实现自定义查询逻辑，避免XML映射文件
 */
@Mapper
public interface SystemDictMapper extends BaseMapper<SystemDict> {
    
    /**
     * 根据字典编码查询字典项列表
     * 
     * 查询逻辑：
     * 1. 根据字典编码精确匹配
     * 2. 只查询未删除的字典项（isDeleted = 0）
     * 3. 只查询启用的字典项（status = 1）
     * 4. 按排序字段排序
     * 5. 返回所有匹配的字典项
     * 
     * @param dictCode 字典编码，如：DICT_1
     * @return 字典项列表，如果不存在则返回空列表
     */
    default List<SystemDict> selectByDictCode(String dictCode) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getDictCode, dictCode)
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder);
        return selectList(query);
    }
    
    /**
     * 根据字典编码查询字典分类（顶级字典）
     * 
     * 查询逻辑：
     * 1. 根据字典编码精确匹配
     * 2. 只查询顶级字典（level = 1）
     * 3. 只查询未删除的字典（isDeleted = 0）
     * 4. 只查询启用的字典（status = 1）
     * 
     * @param dictCode 字典编码，如：DICT_1
     * @return 字典分类对象，如果不存在则返回null
     */
    default SystemDict selectDictCategory(String dictCode) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getDictCode, dictCode)
             .eq(SystemDict::getLevel, 1)
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1);
        return selectOne(query);
    }
    
    /**
     * 根据父级ID查询子字典列表
     * 
     * 查询逻辑：
     * 1. 根据父级ID精确匹配
     * 2. 只查询未删除的字典项（isDeleted = 0）
     * 3. 只查询启用的字典项（status = 1）
     * 4. 按排序字段排序
     * 
     * @param parentId 父级字典ID，0表示查询顶级字典
     * @return 子字典列表，如果不存在则返回空列表
     */
    default List<SystemDict> selectByParentId(Long parentId) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentId)
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder);
        return selectList(query);
    }
    
    /**
     * 根据模块查询字典分类列表
     * 
     * 查询逻辑：
     * 1. 根据模块精确匹配
     * 2. 只查询顶级字典（level = 1）
     * 3. 只查询未删除的字典（isDeleted = 0）
     * 4. 只查询启用的字典（status = 1）
     * 5. 按排序字段排序
     * 
     * @param module 模块名称，如：SYSTEM、USER
     * @return 字典分类列表，如果不存在则返回空列表
     */
    default List<SystemDict> selectCategoriesByModule(String module) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getModule, module)
             .eq(SystemDict::getLevel, 1)
             .eq(SystemDict::getIsDeleted, 0)
             .eq(SystemDict::getStatus, 1)
             .orderByAsc(SystemDict::getSortOrder);
        return selectList(query);
    }
    
    /**
     * 检查字典编码是否存在
     * 
     * 检查逻辑：
     * 1. 根据字典编码精确匹配
     * 2. 只统计未删除的字典（isDeleted = 0）
     * 3. 返回匹配的记录数量
     * 
     * @param dictCode 字典编码
     * @return 匹配的记录数量，0表示不存在，大于0表示已存在
     */
    default Long countByDictCode(String dictCode) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getDictCode, dictCode)
             .eq(SystemDict::getIsDeleted, 0);
        return selectCount(query);
    }
    
    /**
     * 检查字典编码是否存在（排除指定ID）
     * 用于更新时的重复性检查
     * 
     * @param dictCode 字典编码
     * @param excludeId 要排除的字典ID
     * @return 匹配的记录数量，0表示不存在，大于0表示已存在
     */
    default Long countByDictCodeExcludeId(String dictCode, Long excludeId) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getDictCode, dictCode)
             .eq(SystemDict::getIsDeleted, 0)
             .ne(SystemDict::getId, excludeId);
        return selectCount(query);
    }
    
    /**
     * 检查同一父级下字典名称是否存在
     * 
     * @param parentId 父级字典ID
     * @param dictName 字典名称
     * @return 匹配的记录数量，0表示不存在，大于0表示已存在
     */
    default Long countByParentIdAndDictName(Long parentId, String dictName) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentId)
             .eq(SystemDict::getDictName, dictName)
             .eq(SystemDict::getIsDeleted, 0);
        return selectCount(query);
    }
    
    /**
     * 检查同一父级下字典名称是否存在（排除指定ID）
     * 用于更新时的重复性检查
     * 
     * @param parentId 父级字典ID
     * @param dictName 字典名称
     * @param excludeId 要排除的字典ID
     * @return 匹配的记录数量，0表示不存在，大于0表示已存在
     */
    default Long countByParentIdAndDictNameExcludeId(Long parentId, String dictName, Long excludeId) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentId)
             .eq(SystemDict::getDictName, dictName)
             .eq(SystemDict::getIsDeleted, 0)
             .ne(SystemDict::getId, excludeId);
        return selectCount(query);
    }
    
    /**
     * 批量更新字典状态
     * 
     * 更新逻辑：
     * 1. 根据ID列表批量更新状态
     * 2. 同时更新updateTime字段
     * 3. 返回更新成功的记录数量
     * 
     * @param ids 字典ID列表
     * @param status 目标状态，0:禁用，1:启用
     * @return 更新成功的记录数量
     */
    default Integer batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        LambdaUpdateWrapper<SystemDict> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SystemDict::getId, ids)
                    .set(SystemDict::getStatus, status)
                    .set(SystemDict::getUpdateTime, LocalDateTime.now());
        
        return update(null, updateWrapper);
    }
    
    /**
     * 批量更新字典排序
     * 
     * 更新逻辑：
     * 1. 根据ID和排序值的映射关系批量更新
     * 2. 同时更新updateTime字段
     * 3. 返回更新成功的记录数量
     * 
     * @param idSortMap 字典ID和排序值的映射关系
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
            
            LambdaUpdateWrapper<SystemDict> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SystemDict::getSortOrder, sortOrder)
                        .set(SystemDict::getUpdateTime, LocalDateTime.now())
                        .eq(SystemDict::getId, id);
            
            if (update(null, updateWrapper) > 0) {
                updateCount++;
            }
        }
        
        return updateCount;
    }
    
    /**
     * 获取最大顶级字典编码
     * 查询parent_id=0的字典中最大的dict_code（按数字排序）
     * 
     * @return 最大的顶级字典编码，如：DICT_5
     */
    default String getMaxTopLevelCode() {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, 0)
             .eq(SystemDict::getIsDeleted, 0)
             // 使用原生SQL按path的数字部分排序（提取第一级数字）
             .last("ORDER BY CAST(SUBSTRING(path, 1, LOCATE('.', CONCAT(path, '.')) - 1) AS UNSIGNED) DESC LIMIT 1");
        
        SystemDict dict = selectOne(query);
        return dict != null ? dict.getDictCode() : null;
    }
    
    /**
     * 获取指定父级下的最大子级字典编码
     * 查询指定parent_id下最大的dict_code（按最后一级数字排序）
     * 
     * @param parentId 父级字典ID
     * @return 最大的子级字典编码，如：DICT_3.3.4
     */
    default String getMaxChildCodeByParentId(Long parentId) {
        LambdaQueryWrapper<SystemDict> query = new LambdaQueryWrapper<>();
        query.eq(SystemDict::getParentId, parentId)
             .eq(SystemDict::getIsDeleted, 0)
             // 使用原生SQL按path最后一级的数字部分排序
             .last("ORDER BY CAST(SUBSTRING_INDEX(path, '.', -1) AS UNSIGNED) DESC LIMIT 1");
        
        SystemDict dict = selectOne(query);
        return dict != null ? dict.getDictCode() : null;
    }
} 