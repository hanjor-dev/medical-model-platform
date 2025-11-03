package com.okbug.platform.config.db;

import com.okbug.platform.common.constants.SystemConfigKeys;
import com.okbug.platform.entity.system.SystemConfig;
import com.okbug.platform.mapper.system.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 在应用启动时初始化（upsert）关键系统配置项，确保缺失时写入默认值。
 * 只在不存在时创建，已存在的配置不做变更。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigInitializer implements CommandLineRunner {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public void run(String... args) {
        try {
            // 用户类配置
            ensureConfig(
                    SystemConfigKeys.USER_OPERATION_FAIL_MAX_COUNT,
                    "用户受保护操作失败最大次数（触发锁定阈值）",
                    10
            );
            ensureConfig(
                    SystemConfigKeys.USER_OPERATION_LOCK_DURATION_MINUTES,
                    "用户受保护操作触发锁定后的锁定时长（分钟）",
                    11
            );
            ensureConfig(
                    SystemConfigKeys.USER_OPERATION_FAIL_WINDOW_MINUTES,
                    "用户受保护操作失败计数时间窗口（分钟）",
                    12
            );
            ensureConfig(
                    SystemConfigKeys.USER_REGISTER_ENABLED,
                    "是否开启新用户注册",
                    20
            );
            ensureConfig(
                    SystemConfigKeys.USER_REGISTER_REWARD_ENABLED,
                    "是否开启新用户注册奖励积分",
                    21
            );
            ensureConfig(
                    SystemConfigKeys.USER_REFERRAL_REWARD_ENABLED,
                    "是否开启引荐奖励积分",
                    22
            );
            ensureConfig(
                    SystemConfigKeys.USER_DEFAULT_PASSWORD,
                    "默认用户密码（用于重置或初始化）",
                    23
            );
            ensureConfig(
                    SystemConfigKeys.REFERRAL_BASE_URL,
                    "引荐链接基础URL（用于拼装完整引荐链接）",
                    24
            );

            // 系统/缓存
            ensureConfig(
                    SystemConfigKeys.CONFIG_CACHE_TTL,
                    "系统配置Redis缓存TTL（秒）",
                    30
            );

            // 权限特性开关
            ensureConfig(
                    SystemConfigKeys.PERMISSION_ROLE_CONTRIB_SYNC_ENABLED,
                    "是否启用角色权限贡献同步（启用后构建与合并user_permission_contrib）",
                    40
            );

            

            // 团队相关限流与幂等
            ensureConfig(
                    SystemConfigKeys.TEAM_INVITE_RATE_LIMIT_PER_MINUTE,
                    "创建团队邀请-每用户每分钟最大次数",
                    60
            );
            ensureConfig(
                    SystemConfigKeys.TEAM_JOIN_RATE_LIMIT_PER_MINUTE,
                    "提交加入申请-每用户每分钟最大次数",
                    61
            );
            ensureConfig(
                    SystemConfigKeys.TEAM_INVITE_IDEMPOTENCY_TTL_SECONDS,
                    "团队邀请创建幂等键TTL（秒）",
                    62
            );
            ensureConfig(
                    SystemConfigKeys.TEAM_JOIN_IDEMPOTENCY_TTL_SECONDS,
                    "团队加入申请幂等键TTL（秒）",
                    63
            );

            // 子账号迁移与开关
            ensureConfig(
                    SystemConfigKeys.MIGRATION_SUBACCOUNT_TO_TEAM_ENABLED,
                    "是否启用一次性子账号到团队的迁移执行（true时执行一次）",
                    70
            );
            ensureConfig(
                    SystemConfigKeys.SUBACCOUNT_API_ENABLED,
                    "是否启用旧的子账号API（默认false，逐步下线）",
                    71
            );

            log.info("SystemConfigInitializer completed");
        } catch (Exception e) {
            log.warn("SystemConfigInitializer skipped due to error: {}", e.getMessage());
        }
    }

    private void ensureConfig(String key, String description, int sortOrder) {
        if (systemConfigMapper.countByConfigKey(key) > 0) {
            return;
        }
        String defaultValue = SystemConfigKeys.getDefaultValue(key);
        String category = SystemConfigKeys.getConfigCategory(key);
        String categoryCode = SystemConfigKeys.getConfigCategoryCode(key);
        String type = SystemConfigKeys.getConfigType(key);
        String typeCode = SystemConfigKeys.getConfigTypeCode(key);

        SystemConfig c = new SystemConfig();
        c.setConfigKey(key);
        c.setConfigValue(defaultValue);
        c.setConfigCategory(category);
        c.setConfigCategoryCode(categoryCode);
        c.setConfigType(type);
        c.setConfigTypeCode(typeCode);
        c.setDescription(description);
        c.setStatus(1);
        c.setSortOrder(sortOrder);
        c.setIsDeleted(0);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        systemConfigMapper.insert(c);
        log.info("Seed system config: {}={} ({})", key, defaultValue, category);
    }
}


