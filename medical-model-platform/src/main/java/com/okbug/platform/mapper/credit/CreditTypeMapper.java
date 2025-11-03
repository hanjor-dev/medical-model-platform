/**
 * 积分类型Mapper接口：提供积分类型的数据访问方法
 * 
 * 功能描述：
 * 1. 积分类型的CRUD操作
 * 2. 积分类型查询和筛选
 * 3. 支持分页和排序查询
 * 4. 使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:20:00
 */
package com.okbug.platform.mapper.credit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.credit.CreditType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditTypeMapper extends BaseMapper<CreditType> {
    
    /**
     * 分页查询积分类型列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词（类型名称或编码）
     * @param status 状态筛选
     * @return 分页结果
     */
    default IPage<CreditType> selectCreditTypePage(Page<CreditType> page, String keyword, Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("type_name", keyword).or().like("type_code", keyword));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("sort_order", "create_time");
        return selectPage(page, qw);
    }
    
    /**
     * 查询所有启用的积分类型（按排序和创建时间排序）
     * 
     * @return 启用的积分类型列表
     */
    default List<CreditType> selectEnabledCreditTypes() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType>()
                .eq("status", CreditType.STATUS_ENABLED)
                .orderByAsc("sort_order", "create_time"));
    }
    
    /**
     * 根据类型编码查询积分类型
     * 
     * @param typeCode 类型编码
     * @return 积分类型
     */
    default CreditType selectByTypeCode(String typeCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType>()
                .eq("type_code", typeCode));
    }
    
    /**
     * 检查类型编码是否已存在
     * 
     * @param typeCode 类型编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    default boolean existsByTypeCode(String typeCode, Long excludeId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType>()
                        .eq("type_code", typeCode);
        
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查类型名称是否已存在
     * 
     * @param typeName 类型名称
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    default boolean existsByTypeName(String typeName, Long excludeId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType>()
                        .eq("type_name", typeName);
        
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 查询支持转账的积分类型
     * 
     * @return 支持转账的积分类型列表
     */
    default List<CreditType> selectTransferableCreditTypes() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditType>()
                .eq("status", CreditType.STATUS_ENABLED)
                .eq("is_transferable", CreditType.TRANSFER_ENABLED)
                .orderByAsc("sort_order"));
    }
    
    /**
     * 批量更新积分类型排序
     * 
     * @param creditTypes 包含ID和排序的积分类型列表
     * @return 更新成功的记录数
     */
    default int updateBatchSort(List<CreditType> creditTypes) {
        int count = 0;
        for (CreditType creditType : creditTypes) {
            if (creditType.getId() != null && creditType.getSortOrder() != null) {
                count += update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<CreditType>()
                        .set("sort_order", creditType.getSortOrder())
                        .eq("id", creditType.getId()));
            }
        }
        return count;
    }
} 