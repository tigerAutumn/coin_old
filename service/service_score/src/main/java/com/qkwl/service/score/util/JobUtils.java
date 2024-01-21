package com.qkwl.service.score.util;

import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.redis.MemCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/**
 * 工具类
 *
 * @author jany
 */
@Component("utils")
public class JobUtils {

    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private MemCache memCache;

    public BigDecimal getLastPrice(Integer tradeId) {
        return redisHelper.getLastPrice(tradeId);
    }

    public String getRedisData(int db, String key) {
        return memCache.get(db, key);
    }

    public void setRedisData(int db, String key, String value, int expireSeconds) {
        memCache.set(db, key, value, expireSeconds);
    }

}
