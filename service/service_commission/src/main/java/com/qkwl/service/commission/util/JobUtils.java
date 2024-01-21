package com.qkwl.service.commission.util;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.redis.MemCache;

/**
 * 工具类
 *
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
