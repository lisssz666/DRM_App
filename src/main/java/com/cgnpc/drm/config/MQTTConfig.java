package com.cgnpc.drm.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQTTConfig {

    private static final Logger logger = LoggerFactory.getLogger(MQTTConfig.class);

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.keep-alive-interval}")
    private int keepAliveInterval;

    @Bean
    public MqttClient mqttClient() {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            logger.info("MQTT客户端创建成功，broker地址: {}", brokerUrl);
            return client;
        } catch (MqttException e) {
            logger.error("MQTT客户端创建失败: {}", e.getMessage());
            throw new RuntimeException("MQTT客户端初始化失败", e);
        }
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(keepAliveInterval);
        return options;
    }

    @Bean
    public int mqttQos() {
        return qos;
    }

    @Bean
    public String mqttBrokerUrl() {
        return brokerUrl;
    }
}