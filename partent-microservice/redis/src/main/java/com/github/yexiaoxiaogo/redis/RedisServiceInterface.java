package com.github.yexiaoxiaogo.redis;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ListOperations;

/**
 * redis 操作服务接口，可以针对不通业务扩展
 */
public interface RedisServiceInterface {

    /**
     * 是否包含
     */
    boolean containsKey(String key);

    /**
     * 某段时间后Value失效
     * 
     * @param key
     * @param value
     * @param time
     * @return
     */
    boolean expireValue(String key, String value, long time);

    /**
     * 某段时间后Set失效
     * 
     * @param key
     * @param value
     * @param time
     * @return
     */
    boolean expireSet(String key, String value, long time);

    boolean expireSet(String key, Set<String> v, long time);

    /**
     * 在某段时间后List失效
     * 
     * @param key
     * @param v
     * @param time
     * @return
     */
    boolean expireList(String key, String v, long time);

    boolean expireList(String key, List<String> v, long time);

    /**
     * 缓存Value
     * 
     * @param key
     * @param value
     * @return
     */
    boolean cacheValue(String key, String value);

    /**
     * 缓存Set
     * 
     * @param key
     * @param value
     * @return
     */
    boolean cacheSet(String key, String value);

    boolean cacheSet(String key, Set<String> v);

    /**
     * 缓存List
     * 
     * @param key
     * @param v
     * @return
     */
    boolean cacheList(String key, String v);

    boolean cacheList(String key, List<String> v);

    /**
     * 获取缓存
     */
    String getValue(String key);

    /**
     * 获取Set
     */
    Set<String> getSet(String key);

    /**
     * 获取List
     */
    List<String> getList(String key, long start, long end);

    /**
     * 移除缓存
     */
    boolean removeValue(String key);

    boolean removeSet(String key);

    boolean removeList(String key);

    /**
     * 获取页码
     */
    long getListSize(String key);

    long getListSize(ListOperations<String, String> listOps, String key);

    /**
     * 移除list缓存
     */
    boolean removeOneOfList(String key);

    /**
     * 缓存一个key-value键值对，如果key已经存在，休眠5ms重试retryCount次，如果成功，设置该值的超时时间为expiredSeconds
     */
    boolean exclusiveSetWithExpire(String key, Object value, int retryCount, int expiredSeconds);

    boolean exclusiveSetWithExpire(String key, Object value);
}
