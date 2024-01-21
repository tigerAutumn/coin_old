package com.qkwl.service.commission.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.properties.MQProperties;
import com.qkwl.service.commission.mq.MQCommissionListener;

@Configuration
@EnableConfigurationProperties(MQProperties.class)
public class MQConfig {

	@Bean
    public MQTopic mqTopic() {
        return new MQTopic();
    }

    @Bean
    public MQCommissionListener commissionListener() {
        return new MQCommissionListener();
    }

    @Bean
    public Map<Subscription, MessageListener> commissionSubscription(MQCommissionListener commissionListener) {
        Subscription subscription = new Subscription();
        subscription.setTopic(MQTopic.COMMISSION);
        subscription.setExpression("*");
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        subscriptionTable.put(subscription, commissionListener);
        return subscriptionTable;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean commissionConsumer(MQProperties mqProperties, Map<Subscription, MessageListener> commissionSubscription) {
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ConsumerId", mqProperties.getCid().getCommission());
        ConsumerBean consumerBean = new ConsumerBean();
        consumerBean.setProperties(properties);
        consumerBean.setSubscriptionTable(commissionSubscription);
        return consumerBean;
    }

}
