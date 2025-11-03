/**
 * 积分类型管理Service接口：定义积分类型的管理操作
 * 
 * 功能描述：
 * 1. 积分类型的CRUD操作
 * 2. 积分类型状态管理
 * 3. 积分类型排序管理
 * 4. 积分类型查询和筛选
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:05:00
 */
package com.okbug.platform.service.credit;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.credit.request.CreditTypeCreateRequest;
import com.okbug.platform.dto.credit.request.CreditTypeUpdateRequest;
import com.okbug.platform.entity.credit.CreditType;

public interface CreditTypeService {
    
    /**
     * 分页查询积分类型列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词（类型名称或编码）
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<CreditType> getCreditTypePage(Page<CreditType> page, String keyword, Integer status);
    
    /**
     * 获取所有启用的积分类型
     * 
     * @return 启用的积分类型列表
     */
    List<CreditType> getEnabledCreditTypes();
    
    /**
     * 根据ID获取积分类型详情
     * 
     * @param id 积分类型ID
     * @return 积分类型详情
     */
    CreditType getCreditTypeById(Long id);
    
    /**
     * 根据类型编码获取积分类型
     * 
     * @param typeCode 类型编码
     * @return 积分类型
     */
    CreditType getCreditTypeByCode(String typeCode);
    
    /**
     * 创建积分类型
     * 
     * @param request 创建请求
     * @return 创建的积分类型
     */
    CreditType createCreditType(CreditTypeCreateRequest request);
    
    /**
     * 更新积分类型
     * 
     * @param id 积分类型ID
     * @param request 更新请求
     * @return 更新后的积分类型
     */
    CreditType updateCreditType(Long id, CreditTypeUpdateRequest request);
    
    /**
     * 删除积分类型（软删除）
     * 
     * @param id 积分类型ID
     * @return 是否删除成功
     */
    boolean deleteCreditType(Long id);
    
    /**
     * 启用积分类型
     * 
     * @param id 积分类型ID
     * @return 是否启用成功
     */
    boolean enableCreditType(Long id);
    
    /**
     * 禁用积分类型
     * 
     * @param id 积分类型ID
     * @return 是否禁用成功
     */
    boolean disableCreditType(Long id);
    
    /**
     * 更新积分类型状态
     * 
     * @param id 积分类型ID
     * @param enabled 是否启用
     * @return 是否更新成功
     */
    boolean updateCreditTypeStatus(Long id, boolean enabled);
    
    /**
     * 批量更新积分类型排序
     * 
     * @param creditTypes 包含ID和排序的积分类型列表
     * @return 更新成功的记录数
     */
    int updateBatchSort(List<CreditType> creditTypes);
    
    /**
     * 检查类型编码是否已存在
     * 
     * @param typeCode 类型编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByTypeCode(String typeCode, Long excludeId);
    
    /**
     * 获取支持转账的积分类型列表
     * 
     * @return 支持转账的积分类型列表
     */
    List<CreditType> getTransferableCreditTypes();
    
    /**
     * 验证积分类型是否有效
     * 
     * @param id 积分类型ID
     * @return 是否有效
     */
    boolean isValidCreditType(Long id);
    
    /**
     * 验证积分类型编码是否有效
     * 
     * @param typeCode 积分类型编码
     * @return 是否有效
     */
    boolean isValidCreditTypeCode(String typeCode);
} 