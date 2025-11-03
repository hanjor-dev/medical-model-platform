/**
 * 系统配置键常量类：统一管理系统配置的键名
 * 
 * 核心功能：
 * 1. 集中管理所有系统配置键，避免硬编码
 * 2. 提供配置键的语义化访问，提高代码可读性
 * 3. 支持配置键的分类管理，便于维护
 * 4. 减少拼写错误，提高代码质量
 * 
 * 使用方式：
 * ```java
 * // 获取用户操作失败最大次数（通用）
 * Integer maxCount = systemConfigService.getConfigValueAsInt(
 *     SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT, 5);
 * 
 * // 获取用户操作锁定时长（分钟，通用）
 * Integer lockDuration = systemConfigService.getConfigValueAsInt(
 *     SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES, 30);
 * ```
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:45:00
 */
package com.okbug.platform.common.constants;

/**
 * 系统配置键常量类
 * 当前只包含数据库中实际存在的配置项，避免过度设计
 */
public final class SystemConfigKeys {
    
    /**
     * 私有构造函数，防止实例化
     */
    private SystemConfigKeys() {
        throw new UnsupportedOperationException("这是一个常量类，不能实例化");
    }
    
    // ==================== 用户管理配置 ====================
    
    /**
     * 用户操作失败最大次数（通用）
     * 配置类型：NUMBER
     * 配置分类：USER
     * 默认值：5
     * 说明：在任意受保护用户操作（如登录、兑换码）中，连续失败达到此次数后，将触发临时锁定
     */
    public static final String USER_OPERATION_FAIL_MAX_COUNT = "user.operation.fail.max.count";
    
    /**
     * 用户操作锁定时长（分钟，通用）
     * 配置类型：NUMBER
     * 配置分类：USER
     * 默认值：30
     * 说明：触发临时锁定后的锁定时长，单位为分钟
     */
    public static final String USER_OPERATION_LOCK_DURATION_MINUTES = "user.operation.lock.duration.minutes";
    
    /**
     * 用户操作失败计数窗口（分钟，通用）
     * 配置类型：NUMBER
     * 配置分类：USER
     * 默认值：10
     * 说明：在该时间窗口内累计失败次数，超过阈值触发临时锁定
     */
    public static final String USER_OPERATION_FAIL_WINDOW_MINUTES = "user.operation.fail.window.minutes";
    
    /**
     * 是否开启用户注册功能
     * 配置类型：BOOLEAN
     * 配置分类：USER
     * 默认值：true
     * 说明：控制是否允许新用户注册
     */
    public static final String USER_REGISTER_ENABLED = "user.register.enabled";
    
    /**
     * 是否开启新用户注册奖励
     * 配置类型：BOOLEAN
     * 配置分类：USER
     * 默认值：true
     * 说明：控制是否给新注册用户发放奖励积分
     */
    public static final String USER_REGISTER_REWARD_ENABLED = "user.register.reward.enabled";
    
    /**
     * 是否开启引荐奖励
     * 配置类型：BOOLEAN
     * 配置分类：USER
     * 默认值：true
     * 说明：控制是否给推荐人发放推荐奖励积分
     */
    public static final String USER_REFERRAL_REWARD_ENABLED = "user.referral.reward.enabled";
    
    /**
     * 默认用户密码
     * 配置类型：STRING
     * 配置分类：USER
     * 默认值：Abc12345
     * 说明：用于重置密码或新用户默认密码
     */
    public static final String USER_DEFAULT_PASSWORD = "user.default.password";
    
    /**
     * 引荐码基础链接
     * 配置类型：STRING
     * 配置分类：USER
     * 默认值：http://localhost:8081/register?refCode=
     * 说明：引荐码基础链接，用于生成完整的引荐链接
     */
    public static final String REFERRAL_BASE_URL = "referral.base.url";
    
    // ==================== 缓存管理配置 ====================
    
    /**
     * 配置缓存TTL（秒）
     * 配置类型：NUMBER
     * 配置分类：SYSTEM
     * 默认值：3600
     * 说明：系统配置缓存的过期时间，单位为秒
     */
    public static final String CONFIG_CACHE_TTL = "config.cache.ttl";

    

    // ==================== 团队行为限流与幂等 ====================
    /** 创建邀请-每用户每分钟最大次数 */
    public static final String TEAM_INVITE_RATE_LIMIT_PER_MINUTE = "team.invite.rate.per.minute";
    /** 提交加入申请-每用户每分钟最大次数 */
    public static final String TEAM_JOIN_RATE_LIMIT_PER_MINUTE = "team.join.rate.per.minute";
    /** 邀请创建幂等TTL（秒） */
    public static final String TEAM_INVITE_IDEMPOTENCY_TTL_SECONDS = "team.invite.idem.ttl.seconds";
    /** 加入申请幂等TTL（秒） */
    public static final String TEAM_JOIN_IDEMPOTENCY_TTL_SECONDS = "team.join.idem.ttl.seconds";
    

    // ==================== 团队邀请链接 ====================
    /** 团队邀请基础链接（拼接 teamCode 或 token） */
    public static final String TEAM_INVITE_BASE_URL = "team.invite.base.url";

    // ==================== 权限同步特性开关 ====================
    /**
     * 角色权限贡献同步开关
     * 配置类型：BOOLEAN
     * 配置分类：SYSTEM
     * 默认值：false
     * 说明：启用后，角色权限变更将重建 user_permission_contrib，并在读取时合并贡献
     */
    public static final String PERMISSION_ROLE_CONTRIB_SYNC_ENABLED = "permission.sync.role-contribution.enabled";
    
    // ==================== 子账号迁移/开关 ====================
    /** 是否启用“子账号 -> 团队”迁移执行器（一次性开关） */
    public static final String MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED = "migration.subaccount.team.enabled";
    /** 是否继续暴露子账号相关API（默认关闭） */
    public static final String SUBACCOUNT_API_ENABLED = "system.subaccount.api.enabled";

    // ==================== 工具方法 ====================
    
    /**
     * 获取配置键的分类
     * 
     * @param configKey 配置键
     * @return 配置分类，如果无法确定则返回"SYSTEM"
     */
    public static String getConfigCategory(String configKey) {
        if (configKey == null) {
            return "SYSTEM";
        }
        
        if (configKey.startsWith("user.")) {
            return "USER";
        } else if (configKey.startsWith("config.")) {
            return "SYSTEM";
        } else {
            return "SYSTEM";
        }
    }
    
    /**
     * 获取配置键的类型
     * 
     * @param configKey 配置键
     * @return 配置类型，如果无法确定则返回"STRING"
     */
    public static String getConfigType(String configKey) {
        if (configKey == null) {
            return "STRING";
        }
        
        // 根据配置键名称推断类型
        if (configKey.contains("count") || configKey.contains("duration") || 
            configKey.contains("ttl")) {
            return "NUMBER";
        } else if (configKey.contains("enabled")) {
            return "BOOLEAN";
        } else {
            return "STRING";
        }
    }
    
    /**
     * 获取配置键的类型字典编码（DICT_3.*）
     */
    public static String getConfigTypeCode(String configKey) {
        String type = getConfigType(configKey);
        switch (type) {
            case "NUMBER":
                return "DICT_3.3";
            case "BOOLEAN":
                return "DICT_3.4";
            case "JSON":
                return "DICT_3.5";
            case "STRING":
            default:
                return "DICT_3.2";
        }
    }
    
    /**
     * 获取配置键的分类字典编码（DICT_1.*）
     */
    public static String getConfigCategoryCode(String configKey) {
        String category = getConfigCategory(configKey);
        switch (category) {
            case "USER":
                return "DICT_1.7";
            case "SECURITY":
                return "DICT_1.9";
            case "CACHE":
                return "DICT_1.10";
            case "FILE":
                return "DICT_1.11";
            case "TASK":
                return "DICT_1.12";
            case "SYSTEM":
            default:
                return "DICT_1.8";
        }
    }
    
    /**
     * 获取配置键的默认值
     * 
     * @param configKey 配置键
     * @return 默认值，如果无法确定则返回null
     */
    public static String getDefaultValue(String configKey) {
        if (configKey == null) {
            return null;
        }
        
        // 根据配置键返回对应的默认值
        switch (configKey) {
            case USER_OPERATION_FAIL_MAX_COUNT:
                return "5";
            case USER_OPERATION_LOCK_DURATION_MINUTES:
                return "30";
            case USER_OPERATION_FAIL_WINDOW_MINUTES:
                return "10";
            case USER_REGISTER_ENABLED:
                return "true";
            case USER_REGISTER_REWARD_ENABLED:
                return "true";
            case USER_REFERRAL_REWARD_ENABLED:
                return "true";
            case USER_DEFAULT_PASSWORD:
                return "Abc12345";
            case REFERRAL_BASE_URL:
                return "http://localhost:8081/register?refCode=";
            case CONFIG_CACHE_TTL:
                return "3600";
            case PERMISSION_ROLE_CONTRIB_SYNC_ENABLED:
                return "false";
            
            case TEAM_INVITE_RATE_LIMIT_PER_MINUTE:
                return "10";
            case TEAM_JOIN_RATE_LIMIT_PER_MINUTE:
                return "6";
            case TEAM_INVITE_IDEMPOTENCY_TTL_SECONDS:
                return "30";
            case TEAM_JOIN_IDEMPOTENCY_TTL_SECONDS:
                return "30";
            
            case MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED:
                return "false";
            case SUBACCOUNT_API_ENABLED:
                return "false";
            case TEAM_INVITE_BASE_URL:
                return "http://localhost:8081/register?teamCode=";
            default:
                return null;
        }
    }
} 