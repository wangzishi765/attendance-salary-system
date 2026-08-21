package com.example.hrms.common.impl;

import com.example.hrms.common.CacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存实现（基于 ConcurrentHashMap）
 * 当 hrms.cache.enabled=false 时使用，作为 Redis 不可用时的降级方案
 * 支持带过期时间的缓存（惰性删除）
 */
@Service
@ConditionalOnProperty(name = "hrms.cache.enabled", havingValue = "false")
public class LocalCacheServiceImpl implements CacheService {

    /**
     * 缓存条目，包含值和过期时间
     */
    private static class CacheEntry {
        Object value;
        long expireAt; // 过期时间戳，0表示永不过期

        CacheEntry(Object value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return expireAt > 0 && System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value) {
        cache.put(key, new CacheEntry(value, 0));
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        long expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        cache.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return (T) entry.value;
    }

    @Override
    public boolean delete(String key) {
        return cache.remove(key) != null;
    }

    @Override
    public boolean hasKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public void deleteByPrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @Override
    public String getCacheType() {
        return "local";
    }
}
