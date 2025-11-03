package com.okbug.platform.service.permission.impl;

import com.okbug.platform.service.permission.PermissionVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionVersionServiceImpl implements PermissionVersionService {

    private static final String KEY = "permission:version:global";

    private final StringRedisTemplate redisTemplate;

    /**
     * 递增全局权限版本（用于前端/缓存快速失效判定）。
     *
     * @return 递增后的版本号，失败时返回0
     */
    @Override
    public long incrementGlobalVersion() {
        Long v = redisTemplate.opsForValue().increment(KEY);
        long val = v == null ? 0L : v;
        log.debug("Permission version incremented to {}", val);
        return val;
    }

    /**
     * 获取全局权限版本号。
     *
     * @return 当前版本号，未设置时返回0
     */
    @Override
    public long getGlobalVersion() {
        String val = redisTemplate.opsForValue().get(KEY);
        long parsed = val == null ? 0L : Long.parseLong(val);
        log.debug("Permission version read = {}", parsed);
        return parsed;
    }
}


