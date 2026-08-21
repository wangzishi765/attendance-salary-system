package com.example.hrms.common.impl;

import com.example.hrms.common.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存实现
 * 当 hrms.cache.enabled=true 时使用，Redis 连接失败时自动降级不影响业务
 */
@Service
@ConditionalOnProperty(name = "hrms.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("Redis set失败，key={}, 错误={}", key, e.getMessage());
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis set失败，key={}, 错误={}", key, e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get失败，key={}, 错误={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.warn("Redis delete失败，key={}, 错误={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis hasKey失败，key={}, 错误={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis deleteByPrefix失败，prefix={}, 错误={}", prefix, e.getMessage());
        }
    }

    @Override
    public String getCacheType() {
        return "redis";
    }
}
