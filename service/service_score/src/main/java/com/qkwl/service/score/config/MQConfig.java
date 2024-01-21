package com.qkwl.service.score.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.qkwl.common.mq.MQConstant;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.properties.MQProperties;
import com.qkwl.service.score.mq.MQUserScoreListener;
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

    @Bean
    public MQUserScoreListener scoreListener() {
        return new MQUserScoreListener();
    }

    @Bean
    public Map<Subscription, MessageListener>  scoreSubscription(MQUserScoreListener scoreListener) {
        Subscription subscription = new Subscription();
        subscription.setTopic(MQTopic.SCORE);
        subscription.setExpression("*");
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        subscriptionTable.put(subscription, scoreListener);
        return subscriptionTable;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean scoreConsumer(MQProperties mqProperties, Map<Subscription, MessageListener> scoreSubscription) {
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ConsumerId", mqProperties.getCid().getScore());
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(properties);
        consumerBean.setSubscriptionTable(scoreSubscription);
        return consumerBean;
    }
}
