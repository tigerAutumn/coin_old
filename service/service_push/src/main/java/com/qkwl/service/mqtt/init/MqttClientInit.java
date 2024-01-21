package com.qkwl.service.mqtt.init;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qkwl.service.mqtt.config.MqttProperties;
import com.qkwl.service.mqtt.signature.MqttSignature;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttClientInit {

	@Bean
    public MqttClient mqttClient(MqttProperties mqttProperties) {
		MemoryPersistence persistence = new MemoryPersistence();
    	
    	System.out.printf(mqttProperties.getUrl());
    	
    	MqttClient mqttClient = null;
    	
    	String broker = mqttProperties.getUrl() + ":" + mqttProperties.getPort();
    	//String broker ="tcp://post-cn-4590o3eqj0v.mqtt.aliyuncs.com:1883";
    	String clientId =mqttProperties.getClientId();
    	
    	try {
    		String sign;
    		mqttClient = new MqttClient(broker, clientId, persistence);

    		final MqttConnectOptions connOpts = new MqttConnectOptions();
            System.out.println("Connecting to broker: " + broker);
            /**
             * 计算签名，将签名作为 MQTT 的 password。
             * 签名的计算方法，参考工具类 MacSignature，第一个参数是 ClientID 的前半部分，即 GroupID
             * 第二个参数阿里云的 SecretKey
             */
            sign = MqttSignature.macSignature(clientId.split("@@@")[0], mqttProperties.getSecretKey());
            connOpts.setUserName(mqttProperties.getAcessKey());
            connOpts.setServerURIs(new String[] { broker });
            connOpts.setPassword(sign.toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(90);
            connOpts.setAutomaticReconnect(true);
            mqttClient.setCallback(new MqttCallbackExtended() {
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("connect success");
                    //连接成功，需要上传客户端所有的订阅关系
                    
                } 
                public void connectionLost(Throwable throwable) {
                	System.out.println("mqtt connection lost");
                    throwable.printStackTrace();
                }
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    System.out.println("messageArrived:" + topic + "------" + new String(mqttMessage.getPayload()));
                }
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//                    System.out.println("deliveryComplete:" + iMqttDeliveryToken.getMessageId()+iMqttDeliveryToken.getTopics());
                }
            });
            mqttClient.connect(connOpts);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        return mqttClient;
    }
	
	
	
	
}
