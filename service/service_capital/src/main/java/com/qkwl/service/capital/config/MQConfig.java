package com.qkwl.service.capital.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.qkwl.common.framework.mq.ScoreHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.properties.MQProperties;
import com.qkwl.service.capital.mq.MQC2CStatusListener;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author jany
 * @Date 17-4-20
 */
@Configuration
@EnableConfigurationProperties(MQProperties.class)
public class MQConfig {

    @Bean
    public MQTopic mqTopic() {
        return new MQTopic();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean userActionProducer(MQProperties mqProperties) {
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getUserAction());
        producerBean.setProperties(properties);
        return producerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean validateProducer(MQProperties mqProperties) {
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getValidate());
        producerBean.setProperties(properties);
        return producerBean;
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean scoreProducer(MQProperties mqProperties){
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getScore());
        producerBean.setProperties(properties);
        return producerBean;
    }

    @Bean
    public ValidateHelper validateHelper(RedisHelper redisHelper,ProducerBean validateProducer){
        ValidateHelper validateHelper = new ValidateHelper();
        validateHelper.setRedisHelper(redisHelper);
        validateHelper.setValidateProducer(validateProducer);
        return validateHelper;
    }

    @Bean
    public ScoreHelper scoreHelper(ProducerBean scoreProducer){
        ScoreHelper scoreHelper = new ScoreHelper();
        scoreHelper.setScoreProducer(scoreProducer);
        return scoreHelper;
    }

    
    @Bean
    public MQC2CStatusListener entrustStateListener() {
        return new MQC2CStatusListener();
    }

    @Bean
    public Map<Subscription, MessageListener>  c2cStatusSubscription(MQC2CStatusListener mqc2cStatusListener) {
        Subscription subscription = new Subscription();
        subscription.setTopic(MQTopic.c2cEntrustStatus);
        subscription.setExpression("*");
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        subscriptionTable.put(subscription, mqc2cStatusListener);
        return subscriptionTable;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean c2cEentrustStatusConsumer(MQProperties mqProperties, Map<Subscription, MessageListener> c2cStatusSubscription) {
    	System.out.println(mqProperties.getCid().getC2cEntrustStatus());
    	System.out.println(mqProperties.getCid().getEntrustState());
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ConsumerId", mqProperties.getCid().getC2cEntrustStatus());
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(properties);
        consumerBean.setSubscriptionTable(c2cStatusSubscription);
        return consumerBean;
    }
}
