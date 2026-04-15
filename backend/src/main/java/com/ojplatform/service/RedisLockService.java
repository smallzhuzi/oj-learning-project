package com.ojplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Redis 锁相关业务接口。
 */
@Service
public class RedisLockService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """,
            Long.class
    );

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void executeWithLock(String key, Duration ttl, Runnable action) {
        executeWithLock(key, ttl, () -> {
            action.run();
            return null;
        });
    }

    public <T> T executeWithLock(String key, Duration ttl, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        if (!Boolean.TRUE.equals(locked)) {
            throw new RuntimeException("操作过于频繁，请稍后重试");
        }

        try {
            return action.get();
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        }
    }
}
