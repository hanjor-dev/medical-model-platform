/**
 * 积分使用场景Service接口：定义积分使用场景的管理操作
 * 
 * 功能描述：
 * 1. 积分使用场景的CRUD操作
 * 2. 使用场景状态管理
 * 3. 使用场景规则配置
 * 4. 使用场景查询和筛选
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:05:00
 */
package com.okbug.platform.service.credit;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.credit.request.CreditScenarioCreateRequest;
import com.okbug.platform.dto.credit.request.CreditScenarioUpdateRequest;
import com.okbug.platform.entity.credit.CreditUsageScenario;

public interface CreditScenarioService {
    
    /**
     * 分页查询使用场景列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词（场景名称或编码）
     * @param creditTypeCode 积分类型编码筛选
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<CreditUsageScenario> getScenarioPage(Page<CreditUsageScenario> page, String keyword, String creditTypeCode, Integer status);
    
    /**
     * 获取所有启用的使用场景
     * 
     * @return 启用的使用场景列表
     */
    List<CreditUsageScenario> getEnabledScenarios();
    
    /**
     * 根据ID获取使用场景详情
     * 
     * @param id 使用场景ID
     * @return 使用场景详情
     */
    CreditUsageScenario getScenarioById(Long id);
    
    /**
     * 根据场景编码获取使用场景
     * 
     * @param scenarioCode 场景编码
     * @return 使用场景
     */
    CreditUsageScenario getScenarioByCode(String scenarioCode);
    
    /**
     * 创建使用场景
     * 
     * @param request 创建请求
     * @return 创建的使用场景
     */
    CreditUsageScenario createScenario(CreditScenarioCreateRequest request);
    
    /**
     * 更新使用场景
     * 
     * @param id 使用场景ID
     * @param request 更新请求
     * @return 更新后的使用场景
     */
    CreditUsageScenario updateScenario(Long id, CreditScenarioUpdateRequest request);
    
    /**
     * 删除使用场景（软删除）
     * 
     * @param id 使用场景ID
     * @return 是否删除成功
     */
    boolean deleteScenario(Long id);
    
    /**
     * 启用使用场景
     * 
     * @param id 使用场景ID
     * @return 是否启用成功
     */
    boolean enableScenario(Long id);
    
    /**
     * 禁用使用场景
     * 
     * @param id 使用场景ID
     * @return 是否禁用成功
     */
    boolean disableScenario(Long id);
    
    /**
     * 更新使用场景状态
     * 
     * @param id 使用场景ID
     * @param enabled 是否启用
     * @return 是否更新成功
     */
    boolean updateScenarioStatus(Long id, boolean enabled);
    
    /**
     * 根据积分类型编码查询使用场景
     * 
     * @param creditTypeCode 积分类型编码
     * @return 使用场景列表
     */
    List<CreditUsageScenario> getScenariosByCreditType(String creditTypeCode);
    
    /**
     * 查询消费场景（消耗积分的场景）
     * 
     * @return 消费场景列表
     */
    List<CreditUsageScenario> getConsumptionScenarios();
    
    /**
     * 查询奖励场景（获得积分的场景）
     * 
     * @return 奖励场景列表
     */
    List<CreditUsageScenario> getRewardScenarios();
    
    /**
     * 根据用户角色查询可用的使用场景
     * 
     * @param userRole 用户角色
     * @return 可用的使用场景列表
     */
    List<CreditUsageScenario> getScenariosByUserRole(String userRole);
    
    /**
     * 检查场景编码是否已存在
     * 
     * @param scenarioCode 场景编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByScenarioCode(String scenarioCode, Long excludeId);
    
    /**
     * 验证使用场景是否有效
     * 
     * @param id 使用场景ID
     * @return 是否有效
     */
    boolean isValidScenario(Long id);
    
    /**
     * 验证使用场景编码是否有效
     * 
     * @param scenarioCode 使用场景编码
     * @return 是否有效
     */
    boolean isValidScenarioCode(String scenarioCode);
    
    /**
     * 检查用户是否可以使用指定场景
     * 
     * @param userId 用户ID
     * @param scenarioCode 使用场景编码
     * @return 是否可以使用
     */
    boolean canUserUseScenario(Long userId, String scenarioCode);
    
    /**
     * 检查用户是否超过每日使用限制
     * 
     * @param userId 用户ID
     * @param scenarioCode 使用场景编码
     * @return 是否超过限制
     */
    boolean isDailyLimitExceeded(Long userId, String scenarioCode);
} 