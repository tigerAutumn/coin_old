package com.qkwl.service.validate.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.qkwl.common.mq.MQConstant;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.properties.MQProperties;
import com.qkwl.service.validate.mq.MQValidateListener;
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
    public MQValidateListener validateListener() {
        return new MQValidateListener();
    }

    @Bean
    public Map<Subscription, MessageListener> validateSubscription(MQValidateListener validateListener) {
        Subscription subscription = new Subscription();
        subscription.setTopic(MQTopic.VALIDATE);
        subscription.setExpression("*");
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        subscriptionTable.put(subscription, validateListener);
        return subscriptionTable;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean validateConsumer(MQProperties mqProperties, Map<Subscription, MessageListener> validateSubscription) {
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ConsumerId", mqProperties.getCid().getValidate());
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(properties);
        consumerBean.setSubscriptionTable(validateSubscription);
        return consumerBean;
    }
}
