package com.github.yexiaoxiaogo.redis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

/**
 * 默认redis操作服务类
 */
@Repository
public class RedisService implements RedisServiceInterface {
    private final RedisTemplate<String, String> redisTemplate;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在某段时间后失效
     * 
     * @param key key
     * @param v value
     * @param time time
     * @return boolean
     */
    @Override
    public boolean expireValue(String key, String v, long time) {
        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set(key, v);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
                return true;
            }
        } catch (Throwable t) {
            logger.error("缓存[" + key + "]失败, value[" + v + "]", t);
        }
        return false;
    }

    @Override
    public boolean exclusiveSetWithExpire(String key, Object value) {
        return exclusiveSetWithExpire(key, value, 10, 9);
    }

    /**
     * 缓存一个key-value键值对，如果key已经存在，休眠5ms重试retryCount次， 如果成功，设置该值的超时时间为expiredSeconds 默认重试10次，如果始终失败，该函数需要占用线程约100ms时间
     * 
     * @param key
     * @param value
     * @param expiredSeconds 等待超时的时间
     * @return 是否设置成功
     */
    @Override
    public boolean exclusiveSetWithExpire(String key, Object value, int retryCount, int expiredSeconds) {
        if (retryCount <= 0) {
            retryCount = 10;
        }

        int count = 0;
        boolean result = false;
        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            while (count <= retryCount) {
                // Redis一个来回大约占用5ms
                result = valueOps.setIfAbsent(key, value.toString());
                if (result) {
                    break;
                }

                // 如果没有成功，休眠5ms（相当于另一个操作实例操作redis的时间）
                Thread.sleep(5);
                count++;
            }
            if (result) {
                valueOps.getOperations().expire(key, expiredSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("redis exception :", e);
        }
        return result;
    }

    /**
     * 缓存value操作
     * 
     * @param key key
     * @param v value
     * @return boolean
     */
    @Override
    public boolean cacheValue(String key, String v) {
        return expireValue(key, v, -1);
    }

    @Override
    public boolean containsKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Throwable t) {
            logger.error("判断缓存存在失败key[" + key + ", Codeor[" + t + "]");
        }
        return false;
    }

    /**
     * 获取缓存
     *
     * @param key key
     * @return string
     */
    @Override
    public String getValue(String key) {
        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            return valueOps.get(key);
        } catch (Throwable t) {
            logger.error("获取缓存失败key[" + key + ", Codeor[" + t + "]");
        }
        return null;
    }

    /**
     * 移除缓存
     *
     * @param key key
     * @return boolean
     */
    @Override
    public boolean removeValue(String key) {
        return remove(key);
    }

    @Override
    public boolean removeSet(String key) {
        return remove(key);
    }

    @Override
    public boolean removeList(String key) {
        return remove(key);
    }

    /**
     * 如果time>0 在某段时间后失效
     * 
     * @param key key
     * @param v value
     * @param time time
     * @return boolean
     */
    @Override
    public boolean expireSet(String key, String v, long time) {
        try {
            SetOperations<String, String> valueOps = redisTemplate.opsForSet();
            valueOps.add(key, v);
            if (time > 0)
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } catch (Throwable t) {
            logger.error("缓存[" + key + "]失败, value[" + v + "]", t);
        }
        return false;
    }

    /**
     * 缓存set
     *
     * @param key key
     * @param v value
     * @return boolean
     */
    @Override
    public boolean cacheSet(String key, String v) {
        return expireSet(key, v, -1);
    }

    /**
     * 缓存set
     *
     * @param key key
     * @param v value
     * @param time time
     * @return boolean
     */
    @Override
    public boolean expireSet(String key, Set<String> v, long time) {
        try {
            SetOperations<String, String> setOps = redisTemplate.opsForSet();
            setOps.add(key, v.toArray(new String[v.size()]));
            if (time > 0)
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } catch (Throwable t) {
            logger.error("缓存[" + key + "]失败, value[" + v + "]", t);
        }
        return false;
    }

    /**
     * 缓存set
     *
     * @param key key
     * @param v value
     * @return boolean
     */
    @Override
    public boolean cacheSet(String key, Set<String> v) {
        return expireSet(key, v, -1);
    }

    /**
     * 获取缓存set数据
     *
     * @param key key
     * @return set
     */
    @Override
    public Set<String> getSet(String key) {
        try {
            SetOperations<String, String> setOps = redisTemplate.opsForSet();
            return setOps.members(key);
        } catch (Throwable t) {
            logger.error("获取set缓存失败key[" + key + ", Codeor[" + t + "]");
        }
        return null;
    }

    /**
     * list缓存
     *
     * @param key key
     * @param v value
     * @param time time
     * @return boolean
     */
    @Override
    public boolean expireList(String key, String v, long time) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPush(key, v);
            if (time > 0)
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } catch (Throwable t) {
            logger.error("缓存[" + key + "]失败, value[" + v + "]", t);
        }
        return false;
    }

    /**
     * 缓存list
     *
     * @param key key
     * @param v value
     * @return boolean
     */
    @Override
    public boolean cacheList(String key, String v) {
        return expireList(key, v, -1);
    }

    /**
     * 缓存list
     *
     * @param key key
     * @param v value
     * @param time time
     * @return boolean
     */
    @Override
    public boolean expireList(String key, List<String> v, long time) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPushAll(key, v);
            if (time > 0)
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } catch (Throwable t) {
            logger.error("缓存[" + key + "]失败, value[" + v + "]", t);
        }
        return false;
    }

    /**
     * 缓存list
     *
     * @param key key
     * @param v value
     * @return boolean
     */
    @Override
    public boolean cacheList(String key, List<String> v) {
        return expireList(key, v, -1);
    }

    /**
     * 获取list缓存
     *
     * @param key key
     * @param start start
     * @param end end
     * @return list
     */
    @Override
    public List<String> getList(String key, long start, long end) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            return listOps.range(key, start, end);
        } catch (Throwable t) {
            logger.error("获取list缓存失败key[" + key + ", Codeor[" + t + "]");
        }
        return null;
    }

    /**
     * 获取总条数, 可用于分页
     *
     * @param key key
     * @return long
     */
    @Override
    public long getListSize(String key) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            return listOps.size(key);
        } catch (Throwable t) {
            logger.error("获取list长度失败key[" + key + "], Codeor[" + t + "]");
        }
        return 0;
    }

    /**
     * 获取总条数, 可用于分页
     *
     * @param listOps listOps
     * @param key key
     * @return long
     */
    @Override
    public long getListSize(ListOperations<String, String> listOps, String key) {
        try {
            return listOps.size(key);
        } catch (Throwable t) {
            logger.error("获取list长度失败key[" + key + "], Codeor[" + t + "]");
        }
        return 0;
    }

    /**
     * 移除list缓存
     *
     * @param key key
     * @return boolean
     */
    @Override
    public boolean removeOneOfList(String key) {
        try {
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPop(key);
            return true;
        } catch (Throwable t) {
            logger.error("移除list缓存失败key[" + key + ", Codeor[" + t + "]");
        }
        return false;
    }

    /**
     * 移除缓存
     *
     * @param key key
     * @return boolean
     */
    private boolean remove(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Throwable t) {
            logger.error("获取缓存失败key[" + key + ", Codeor[" + t + "]");
        }
        return false;
    }
}
