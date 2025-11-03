package com.okbug.platform.service.security;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 防暴力尝试保护：统计失败次数并在阈值后临时锁定，必要时强制下线。
 * 适用于兑换码、登录、短信验证码等需要限制错误重试的功能。
 */
@Component
public class BruteForceGuard {

    private static final String KEY_PREFIX = "sec:bf:"; // base key

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static class Policy {
        public final int maxFailures;              // 最大失败次数
        public final Duration failureTtl;          // 失败计数过期时间窗口
        public final Duration lockDuration;        // 锁定时长
        public final boolean kickoutOnLock;        // 锁定时是否强制下线

        public Policy(int maxFailures, Duration failureTtl, Duration lockDuration, boolean kickoutOnLock) {
            this.maxFailures = maxFailures;
            this.failureTtl = failureTtl;
            this.lockDuration = lockDuration;
            this.kickoutOnLock = kickoutOnLock;
        }
    }

    /**
     * 检查是否被锁定
     */
    public boolean isLocked(String scene, long userId) {
        String lockKey = lockKey(scene, userId);
        String v = stringRedisTemplate.opsForValue().get(lockKey);
        return v != null;
    }

    /**
     * 记录一次失败，并在达到阈值时锁定
     */
    public void recordFailure(String scene, long userId, Policy policy) {
        String key = failKey(scene, userId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, policy.failureTtl);
        }
        if (count != null && count >= policy.maxFailures) {
            // 锁定
            String lockKey = lockKey(scene, userId);
            stringRedisTemplate.opsForValue().set(lockKey, "1", policy.lockDuration);
            if (policy.kickoutOnLock) {
                try { StpUtil.logout(userId); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 重置失败计数（成功后调用）
     */
    public void reset(String scene, long userId) {
        stringRedisTemplate.delete(failKey(scene, userId));
        // 不主动删除锁定，等待过期
    }

    private String failKey(String scene, long userId) {
        return KEY_PREFIX + scene + ":fail:" + userId;
    }

    private String lockKey(String scene, long userId) {
        return KEY_PREFIX + scene + ":lock:" + userId;
    }
}


