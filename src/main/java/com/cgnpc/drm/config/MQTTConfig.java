package com.cgnpc.drm.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cgnpc.drm.service.MQTTMessageHandlerService;

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

    @Autowired
    private MQTTMessageHandlerService mqttMessageHandlerService;

    @Bean
    public MqttClient mqttClient(MqttConnectOptions options) {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            logger.info("MQTT客户端创建成功，broker地址: {}", brokerUrl);

            // 设置回调
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    logger.info("MQTT客户端连接成功，broker地址: {}", serverURI);
                    // 订阅设备状态主题
                    try {
                        client.subscribe("spray/+/status", qos);
                        logger.info("已订阅设备状态主题: spray/+/status");
                    } catch (MqttException e) {
                        logger.error("订阅主题失败: {}", e.getMessage());
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT连接丢失: {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // 处理接收到的消息
                    mqttMessageHandlerService.handleMessage(topic, message);
                }

                @Override
                public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                    // 消息发送完成回调
                }
            });

            // 初始化时建立连接
            client.connect(options);
            return client;
        } catch (MqttException e) {
            logger.error("MQTT客户端创建或连接失败: {}", e.getMessage());
            throw new RuntimeException("MQTT client initialization failed.", e);  // MQTT客户端初始化失败
        }
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(false); // 设置为false，保持会话持久性
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