package com.okbug.platform.common.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redis;

    /**
     * 简单固定窗口限流：在窗口期内累计计数，超过阈值返回false。
     */
    public boolean allow(String key, int limit, Duration window) {
        try {
            Long c = redis.opsForValue().increment(key);
            if (c != null && c == 1L) {
                redis.expire(key, window);
            }
            return c == null || c <= limit;
        } catch (Exception e) {
            log.warn("RateLimit degraded, key {}: {}", key, e.getMessage());
            return true; // 降级放行
        }
    }
}


