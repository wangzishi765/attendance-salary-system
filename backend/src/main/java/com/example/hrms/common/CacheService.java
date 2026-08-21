package com.example.hrms.common;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 抽象缓存操作，支持 Redis 缓存和本地缓存两种实现，自动降级
 */
public interface CacheService {

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存（带过期时间）
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     * @param key 缓存键
     * @param <T> 泛型
     * @return 缓存值，不存在返回 null
     */
    <T> T get(String key);

    /**
     * 删除缓存
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 判断缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 批量删除缓存（按前缀）
     * @param prefix 键前缀
     */
    void deleteByPrefix(String prefix);

    /**
     * 获取缓存类型名称
     * @return redis / local
     */
    String getCacheType();
}
