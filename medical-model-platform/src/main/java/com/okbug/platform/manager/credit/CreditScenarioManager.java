/**
 * 积分场景配置管理器：统一管理积分使用场景的动态配置
 * 
 * 功能描述：
 * 1. 提供积分场景配置的缓存管理
 * 2. 支持动态配置热更新
 * 3. 统一场景配置的访问接口
 * 4. 优化数据库查询性能
 * 
 * 核心特性：
 * - 内存缓存 + 定时刷新
 * - 类型安全的场景编码访问
 * - 配置变更事件通知
 * - 降级策略支持
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-16 10:45:00
 */
package com.okbug.platform.manager.credit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;
import com.okbug.platform.mapper.credit.CreditUsageScenarioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CreditScenarioManager {
    
    @Autowired
    private CreditUsageScenarioMapper creditScenarioMapper;
    
    /**
     * 场景配置缓存：scenarioCode -> CreditUsageScenario
     */
    private final Map<String, CreditUsageScenario> scenarioCache = new ConcurrentHashMap<>();
    
    /**
     * 奖励场景缓存：便于快速查找奖励场景
     */
    private final Map<String, CreditUsageScenario> rewardScenarioCache = new ConcurrentHashMap<>();
    
    /**
     * 消费场景缓存：便于快速查找消费场景
     */
    private final Map<String, CreditUsageScenario> consumptionScenarioCache = new ConcurrentHashMap<>();
    
    /**
     * 缓存最后更新时间
     */
    private volatile long lastUpdateTime = 0;
    
    // ================ 初始化方法 ================
    
    /**
     * 启动时初始化缓存
     */
    @PostConstruct
    public void initializeCache() {
        log.info("初始化积分场景配置缓存");
        refreshCache();
    }
    
    /**
     * 定时刷新缓存（每5分钟）
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void scheduledRefreshCache() {
        log.debug("定时刷新积分场景配置缓存");
        refreshCache();
    }
    
    /**
     * 手动刷新缓存
     */
    public synchronized void refreshCache() {
        try {
            log.debug("开始刷新积分场景配置缓存");
            
            // 从数据库获取所有启用的场景配置（直接使用Mapper，避免与Service形成循环依赖）
            LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CreditUsageScenario::getStatus, CreditUsageScenario.STATUS_ENABLED);
            queryWrapper.orderByAsc(CreditUsageScenario::getId);
            List<CreditUsageScenario> scenarios = creditScenarioMapper.selectList(queryWrapper);
            
            // 清空缓存
            scenarioCache.clear();
            rewardScenarioCache.clear();
            consumptionScenarioCache.clear();
            
            // 重新构建缓存
            for (CreditUsageScenario scenario : scenarios) {
                String code = scenario.getScenarioCode();
                scenarioCache.put(code, scenario);
                
                // 分类缓存
                if (scenario.isRewardScenario()) {
                    rewardScenarioCache.put(code, scenario);
                } else if (scenario.isConsumptionScenario()) {
                    consumptionScenarioCache.put(code, scenario);
                }
            }
            
            lastUpdateTime = System.currentTimeMillis();
            
            log.info("积分场景配置缓存刷新完成，共加载 {} 个场景，其中奖励场景 {} 个，消费场景 {} 个", 
                scenarios.size(), rewardScenarioCache.size(), consumptionScenarioCache.size());
                
        } catch (Exception e) {
            log.error("刷新积分场景配置缓存失败", e);
        }
    }
    
    // ================ 场景配置查询 ================
    
    /**
     * 根据场景编码获取配置
     * 
     * @param scenarioCode 场景编码
     * @return 场景配置，不存在则返回null
     */
    public CreditUsageScenario getScenarioConfig(String scenarioCode) {
        if (!StringUtils.hasText(scenarioCode)) {
            return null;
        }
        
        CreditUsageScenario scenario = scenarioCache.get(scenarioCode.trim());
        if (scenario == null) {
            log.warn("未找到场景配置，场景编码: {}", scenarioCode);
        }
        
        return scenario;
    }
    
    /**
     * 根据场景编码枚举获取配置
     * 
     * @param scenarioCode 场景编码枚举
     * @return 场景配置，不存在则返回null
     */
    public CreditUsageScenario getScenarioConfig(CreditScenarioCode scenarioCode) {
        if (scenarioCode == null) {
            return null;
        }
        
        return getScenarioConfig(scenarioCode.getCode());
    }
    
    /**
     * 检查场景是否存在且已启用
     * 
     * @param scenarioCode 场景编码
     * @return true:存在且启用 false:不存在或已禁用
     */
    public boolean isScenarioEnabled(String scenarioCode) {
        CreditUsageScenario scenario = getScenarioConfig(scenarioCode);
        return scenario != null && scenario.isEnabled();
    }
    
    /**
     * 检查场景是否存在且已启用
     * 
     * @param scenarioCode 场景编码枚举
     * @return true:存在且启用 false:不存在或已禁用
     */
    public boolean isScenarioEnabled(CreditScenarioCode scenarioCode) {
        if (scenarioCode == null) {
            return false;
        }
        
        return isScenarioEnabled(scenarioCode.getCode());
    }
    
    // ================ 专用查询方法 ================
    
    /**
     * 获取用户推荐奖励场景配置
     * 
     * @return 推荐奖励场景配置，不存在则返回null
     */
    public CreditUsageScenario getUserReferralConfig() {
        return getScenarioConfig(CreditScenarioCode.USER_REFERRAL);
    }
    
    /**
     * 获取用户注册奖励场景配置
     * 
     * @return 注册奖励场景配置，不存在则返回null
     */
    public CreditUsageScenario getUserRegisterConfig() {
        return getScenarioConfig(CreditScenarioCode.USER_REGISTER);
    }
    
    /**
     * 获取AI计算消费场景配置
     * 
     * @return AI计算场景配置，不存在则返回null
     */
    public CreditUsageScenario getAiComputeConfig() {
        return getScenarioConfig(CreditScenarioCode.AI_COMPUTE);
    }
    
    /**
     * 获取所有奖励场景配置
     * 
     * @return 奖励场景配置列表
     */
    public List<CreditUsageScenario> getRewardScenarios() {
        return new ArrayList<>(rewardScenarioCache.values());
    }
    
    /**
     * 获取所有消费场景配置
     * 
     * @return 消费场景配置列表
     */
    public List<CreditUsageScenario> getConsumptionScenarios() {
        return new ArrayList<>(consumptionScenarioCache.values());
    }
    
    // ================ 场景规则验证 ================
    
    /**
     * 验证用户是否有权限使用指定场景
     * 
     * @param scenarioCode 场景编码
     * @param userRole 用户角色
     * @return true:有权限 false:无权限
     */
    public boolean hasPermission(String scenarioCode, String userRole) {
        CreditUsageScenario scenario = getScenarioConfig(scenarioCode);
        if (scenario == null) {
            return false;
        }
        
        return scenario.hasRolePermission(userRole);
    }
    
    /**
     * 验证用户是否有权限使用指定场景
     * 
     * @param scenarioCode 场景编码枚举
     * @param userRole 用户角色
     * @return true:有权限 false:无权限
     */
    public boolean hasPermission(CreditScenarioCode scenarioCode, String userRole) {
        if (scenarioCode == null) {
            return false;
        }
        
        return hasPermission(scenarioCode.getCode(), userRole);
    }
    
    /**
     * 获取场景的积分消耗/奖励数量
     * 
     * @param scenarioCode 场景编码
     * @return 积分数量，消费为正数，奖励为负数，不存在则返回null
     */
    public BigDecimal getScenarioCost(String scenarioCode) {
        CreditUsageScenario scenario = getScenarioConfig(scenarioCode);
        if (scenario == null) {
            return null;
        }
        
        return scenario.getCostPerUse();
    }
    
    /**
     * 获取场景的积分消耗/奖励数量
     * 
     * @param scenarioCode 场景编码枚举
     * @return 积分数量，消费为正数，奖励为负数，不存在则返回null
     */
    public BigDecimal getScenarioCost(CreditScenarioCode scenarioCode) {
        if (scenarioCode == null) {
            return null;
        }
        
        return getScenarioCost(scenarioCode.getCode());
    }
    
    /**
     * 检查场景是否有每日使用限制
     * 
     * @param scenarioCode 场景编码
     * @return true:有限制 false:无限制
     */
    public boolean hasDailyLimit(String scenarioCode) {
        CreditUsageScenario scenario = getScenarioConfig(scenarioCode);
        if (scenario == null) {
            return false;
        }
        
        return scenario.hasDailyLimit();
    }
    
    /**
     * 获取场景的每日使用限制
     * 
     * @param scenarioCode 场景编码
     * @return 每日使用限制，无限制则返回null
     */
    public Integer getDailyLimit(String scenarioCode) {
        CreditUsageScenario scenario = getScenarioConfig(scenarioCode);
        if (scenario == null) {
            return null;
        }
        
        return scenario.getDailyLimit();
    }
    
    // ================ 缓存管理 ================
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScenarios", scenarioCache.size());
        stats.put("rewardScenarios", rewardScenarioCache.size());
        stats.put("consumptionScenarios", consumptionScenarioCache.size());
        stats.put("lastUpdateTime", new Date(lastUpdateTime));
        stats.put("cacheKeys", new ArrayList<>(scenarioCache.keySet()));
        
        return stats;
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        log.info("清空积分场景配置缓存");
        scenarioCache.clear();
        rewardScenarioCache.clear();
        consumptionScenarioCache.clear();
        lastUpdateTime = 0;
    }
    
    /**
     * 预热缓存
     */
    public void warmUpCache() {
        log.info("预热积分场景配置缓存");
        refreshCache();
    }
    
    // ================ 便利方法 ================
    
    /**
     * 获取所有已启用的场景编码列表
     * 
     * @return 场景编码列表
     */
    public List<String> getEnabledScenarioCodes() {
        return scenarioCache.values().stream()
                .filter(CreditUsageScenario::isEnabled)
                .map(CreditUsageScenario::getScenarioCode)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户角色获取可用的场景列表
     * 
     * @param userRole 用户角色
     * @return 可用场景列表
     */
    public List<CreditUsageScenario> getAvailableScenarios(String userRole) {
        if (!StringUtils.hasText(userRole)) {
            return Collections.emptyList();
        }
        
        return scenarioCache.values().stream()
                .filter(scenario -> scenario.isEnabled() && scenario.hasRolePermission(userRole))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否为有效的场景编码
     * 
     * @param scenarioCode 场景编码
     * @return true:有效 false:无效
     */
    public boolean isValidScenarioCode(String scenarioCode) {
        return StringUtils.hasText(scenarioCode) && scenarioCache.containsKey(scenarioCode.trim());
    }
}
