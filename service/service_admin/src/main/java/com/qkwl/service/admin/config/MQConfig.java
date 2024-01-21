package com.qkwl.service.admin.config;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.qkwl.common.framework.mq.ScoreHelper;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.properties.MQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public ProducerBean adminActionProducer(MQProperties mqProperties) {
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getAdminAction());
        producerBean.setProperties(properties);
        System.out.println("service_admin 地址 =======> "+mqProperties.getOnsAddr());
        return producerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean userActionProducer(MQProperties mqProperties){
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getUserAction());
        producerBean.setProperties(properties);
        System.out.println("service_admin 地址 =======> "+mqProperties.getOnsAddr());
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
        System.out.println("service_admin 地址 =======> "+mqProperties.getOnsAddr());
        return producerBean;
    }

    @Bean
    public ScoreHelper scoreHelper(ProducerBean scoreProducer){
        ScoreHelper scoreHelper = new ScoreHelper();
        scoreHelper.setScoreProducer(scoreProducer);
        return scoreHelper;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean c2cEntrustStatusProducer(MQProperties mqProperties){
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.setProperty("AccessKey", mqProperties.getAccessKey());
        properties.setProperty("SecretKey", mqProperties.getSecretKey());
        properties.setProperty("ONSAddr", mqProperties.getOnsAddr());
        properties.setProperty("ProducerId", mqProperties.getPid().getC2cEntrustStatus());
        producerBean.setProperties(properties);
        return producerBean;
    }
}
