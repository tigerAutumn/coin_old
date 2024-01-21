package com.qkwl.admin.layui.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qkwl.admin.layui.oss.OssHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.properties.OssConstantProperties;

@Configuration
@EnableConfigurationProperties(OssConstantProperties.class)
public class OssConfig {

	@Bean
    public OssHelper ossHelper(OssConstantProperties ossConstantProperties, RedisHelper redisHelper) {
		OssHelper ossHelper = new OssHelper();
		ossHelper.setRedisHelper(redisHelper);
		ossHelper.setBucketBase(ossConstantProperties.getBucketBase());
		ossHelper.setSecretKey(ossConstantProperties.getSecretKey());
		ossHelper.setAccessKey(ossConstantProperties.getAccessKey());
		ossHelper.setEndPoint(ossConstantProperties.getOssEndpoint());
		
        return ossHelper;
    }
	
	

}
