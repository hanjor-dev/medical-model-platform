/**
 * 积分使用场景Mapper接口：提供积分使用场景的数据访问方法
 * 
 * 功能描述：
 * 1. 积分使用场景的CRUD操作
 * 2. 使用场景查询和筛选
 * 3. 支持分页和条件查询
 * 4. 使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:25:00
 */
package com.okbug.platform.mapper.credit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditUsageScenarioMapper extends BaseMapper<CreditUsageScenario> {
    
    /**
     * 分页查询使用场景列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词（场景名称或编码）
     * @param creditTypeCode 积分类型编码筛选
     * @param status 状态筛选
     * @return 分页结果
     */
    default IPage<CreditUsageScenario> selectScenarioPage(Page<CreditUsageScenario> page, String keyword, String creditTypeCode, Integer status) {
        return selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .like(keyword != null, "scenario_name", keyword)
                .or()
                .like(keyword != null, "scenario_code", keyword)
                .eq(creditTypeCode != null, "credit_type_code", creditTypeCode)
                .eq(status != null, "status", status)
                .orderByAsc("create_time"));
    }
    
    /**
     * 查询所有启用的使用场景
     * 
     * @return 启用的使用场景列表
     */
    default List<CreditUsageScenario> selectEnabledScenarios() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .eq("status", CreditUsageScenario.STATUS_ENABLED)
                .orderByAsc("create_time"));
    }
    
    /**
     * 根据场景编码查询使用场景
     * 
     * @param scenarioCode 场景编码
     * @return 使用场景
     */
    default CreditUsageScenario selectByScenarioCode(String scenarioCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .eq("scenario_code", scenarioCode));
    }
    
    /**
     * 检查场景编码是否已存在
     * 
     * @param scenarioCode 场景编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    default boolean existsByScenarioCode(String scenarioCode, Long excludeId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                        .eq("scenario_code", scenarioCode);
        
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        
        return selectCount(queryWrapper) > 0;
    }
    
    /**
     * 根据积分类型编码查询使用场景
     * 
     * @param creditTypeCode 积分类型编码
     * @return 使用场景列表
     */
    default List<CreditUsageScenario> selectByCreditTypeCode(String creditTypeCode) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .eq("credit_type_code", creditTypeCode)
                .eq("status", CreditUsageScenario.STATUS_ENABLED)
                .orderByAsc("create_time"));
    }
    
    /**
     * 查询消费场景（消耗积分的场景）
     * 
     * @return 消费场景列表
     */
    default List<CreditUsageScenario> selectConsumptionScenarios() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .gt("cost_per_use", 0)
                .eq("status", CreditUsageScenario.STATUS_ENABLED)
                .orderByAsc("create_time"));
    }
    
    /**
     * 查询奖励场景（获得积分的场景）
     * 
     * @return 奖励场景列表
     */
    default List<CreditUsageScenario> selectRewardScenarios() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .lt("cost_per_use", 0)
                .eq("status", CreditUsageScenario.STATUS_ENABLED)
                .orderByAsc("create_time"));
    }
    
    /**
     * 根据用户角色查询可用的使用场景
     * 
     * @param userRole 用户角色
     * @return 可用的使用场景列表
     */
    default List<CreditUsageScenario> selectScenariosByUserRole(String userRole) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditUsageScenario>()
                .eq("status", CreditUsageScenario.STATUS_ENABLED)
                .like("user_roles", userRole)
                .orderByAsc("create_time"));
    }
} 