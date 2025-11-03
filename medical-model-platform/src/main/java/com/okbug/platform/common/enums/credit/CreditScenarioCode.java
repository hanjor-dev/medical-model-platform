/**
 * 积分使用场景编码枚举：与数据库credit_usage_scenarios表中的scenario_code字段对应
 * 
 * 功能描述：
 * 1. 提供类型安全的场景编码定义
 * 2. 与数据库配置保持一致性
 * 3. 避免硬编码字符串错误
 * 4. 支持场景编码的集中管理
 * 
 * 设计原则：
 * - 枚举值与数据库scenario_code保持完全一致
 * - 提供便利方法进行类型转换和验证
 * - 支持分类管理（消费、奖励、特殊场景）
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-16 10:30:00
 */
package com.okbug.platform.common.enums.credit;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum CreditScenarioCode {
    
    // ================ 消费场景 ================
    
    /**
     * 用户阅片
     */
    READING("READING", "用户阅片", ScenarioType.CONSUMPTION),
    
    /**
     * AI计算
     */
    AI_COMPUTE("AI_COMPUTE", "AI计算", ScenarioType.CONSUMPTION),
    
    /**
     * 文件上传
     */
    FILE_UPLOAD("FILE_UPLOAD", "文件上传", ScenarioType.CONSUMPTION),
    
    /**
     * 普通任务
     */
    TASK_NORMAL("TASK_NORMAL", "普通任务", ScenarioType.CONSUMPTION),
    
    /**
     * 高级任务
     */
    TASK_PREMIUM("TASK_PREMIUM", "高级任务", ScenarioType.CONSUMPTION),
    
    /**
     * 模型查看
     */
    MODEL_VIEW("MODEL_VIEW", "模型查看", ScenarioType.CONSUMPTION),
    
    /**
     * 模型下载
     */
    MODEL_DOWNLOAD("MODEL_DOWNLOAD", "模型下载", ScenarioType.CONSUMPTION),
    
    // ================ 奖励场景 ================
    
    /**
     * 用户注册奖励
     */
    USER_REGISTER("USER_REGISTER", "用户注册", ScenarioType.REWARD),
    
    /**
     * 用户推荐奖励
     */
    USER_REFERRAL("USER_REFERRAL", "用户推荐", ScenarioType.REWARD),
    
    /**
     * 首次登录奖励
     */
    FIRST_LOGIN("FIRST_LOGIN", "首次登录", ScenarioType.REWARD),
    
    /**
     * 每日签到奖励
     */
    DAILY_CHECKIN("DAILY_CHECKIN", "每日签到", ScenarioType.REWARD),
    
    /**
     * 任务完成奖励
     */
    TASK_COMPLETE("TASK_COMPLETE", "任务完成", ScenarioType.REWARD),
    
    /**
     * 模型分享奖励
     */
    MODEL_SHARE("MODEL_SHARE", "模型分享", ScenarioType.REWARD),
    
    // ================ 特殊场景 ================
    
    /**
     * VIP升级
     */
    VIP_UPGRADE("VIP_UPGRADE", "VIP升级", ScenarioType.SPECIAL),
    
    /**
     * 活动奖励
     */
    ACTIVITY_REWARD("ACTIVITY_REWARD", "活动奖励", ScenarioType.SPECIAL),
    
    /**
     * 管理员发放
     */
    ADMIN_GRANT("ADMIN_GRANT", "管理员发放", ScenarioType.SPECIAL),

    /**
     * 兑换码（奖励型；类型与额度来自兑换码本身）
     */
    REDEEM_CODE("REDEEM_CODE", "兑换码", ScenarioType.SPECIAL);
    
    /**
     * 场景编码（与数据库一致）
     */
    private final String code;
    
    /**
     * 场景名称
     */
    private final String name;
    
    /**
     * 场景类型
     */
    private final ScenarioType type;
    
    CreditScenarioCode(String code, String name, ScenarioType type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }
    
    // ================ 静态方法 ================
    
    /**
     * 根据编码获取枚举
     * 
     * @param code 场景编码
     * @return 场景枚举，如果不存在返回null
     */
    public static CreditScenarioCode fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (CreditScenarioCode scenario : values()) {
            if (scenario.code.equals(code.trim())) {
                return scenario;
            }
        }
        
        return null;
    }
    
    /**
     * 验证编码是否有效
     * 
     * @param code 场景编码
     * @return true:有效 false:无效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
    
    /**
     * 获取所有场景编码列表
     * 
     * @return 所有场景编码
     */
    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(CreditScenarioCode::getCode)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据场景类型获取场景列表
     * 
     * @param type 场景类型
     * @return 指定类型的场景列表
     */
    public static List<CreditScenarioCode> getByType(ScenarioType type) {
        return Arrays.stream(values())
                .filter(scenario -> scenario.type == type)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有消费场景
     * 
     * @return 消费场景列表
     */
    public static List<CreditScenarioCode> getConsumptionScenarios() {
        return getByType(ScenarioType.CONSUMPTION);
    }
    
    /**
     * 获取所有奖励场景
     * 
     * @return 奖励场景列表
     */
    public static List<CreditScenarioCode> getRewardScenarios() {
        return getByType(ScenarioType.REWARD);
    }
    
    /**
     * 获取所有特殊场景
     * 
     * @return 特殊场景列表
     */
    public static List<CreditScenarioCode> getSpecialScenarios() {
        return getByType(ScenarioType.SPECIAL);
    }
    
    // ================ 实例方法 ================
    
    /**
     * 判断是否为消费场景
     * 
     * @return true:消费场景 false:非消费场景
     */
    public boolean isConsumption() {
        return this.type == ScenarioType.CONSUMPTION;
    }
    
    /**
     * 判断是否为奖励场景
     * 
     * @return true:奖励场景 false:非奖励场景
     */
    public boolean isReward() {
        return this.type == ScenarioType.REWARD;
    }
    
    /**
     * 判断是否为特殊场景
     * 
     * @return true:特殊场景 false:非特殊场景
     */
    public boolean isSpecial() {
        return this.type == ScenarioType.SPECIAL;
    }
    
    @Override
    public String toString() {
        return this.code;
    }
    
    // ================ 内部枚举 ================
    
    /**
     * 场景类型枚举
     */
    public enum ScenarioType {
        /**
         * 消费场景（消耗积分）
         */
        CONSUMPTION,
        
        /**
         * 奖励场景（获得积分）
         */
        REWARD,
        
        /**
         * 特殊场景（管理或活动）
         */
        SPECIAL
    }
}
