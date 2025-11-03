package com.okbug.platform.common.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试登记幂等键，首次返回true，重复返回false。
     */
    public boolean tryAcquire(String key, long ttlSeconds) {
        try {
            Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(ok);
        } catch (DataAccessException e) {
            log.warn("Idempotency degraded, redis error for key {}: {}", key, e.getMessage());
            return true; // 降级放行，避免影响主流程
        }
    }
}


