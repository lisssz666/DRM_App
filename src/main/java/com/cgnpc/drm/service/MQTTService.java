package com.cgnpc.drm.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQTTService {

    private static final Logger logger = LoggerFactory.getLogger(MQTTService.class);
    private static final String TOPIC_PREFIX = "spray/";
    private static final String TOPIC_SUFFIX = "/ctr";

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttConnectOptions mqttConnectOptions;

    @Autowired
    private int mqttQos;

    @Autowired
    private String mqttBrokerUrl;

    /**
     * 确保MQTT客户端已连接
     */
    private void ensureConnected() throws MqttException {
        if (!mqttClient.isConnected()) {
            logger.info("正在连接MQTT broker: {}", mqttBrokerUrl);
            mqttClient.connect(mqttConnectOptions);
            logger.info("MQTT客户端连接成功: {}", mqttBrokerUrl);
        }
    }

    /**
     * 发送MQTT命令到设备
     * @param deviceId 设备ID
     * @param command 命令内容，如 "lock 0", "light 1", "fan 1"
     */
    public void sendCommand(String deviceId, String command) {
        String topic = TOPIC_PREFIX + deviceId + TOPIC_SUFFIX;
        try {
            // 确保MQTT客户端已连接
            ensureConnected();
            
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(mqttQos);
            mqttClient.publish(topic, message);
            logger.info("发送MQTT命令成功: 设备ID={}, 主题={}, 命令={}", deviceId, topic, command);
        } catch (MqttException e) {
            logger.error("发送MQTT命令失败: 设备ID={}, 主题={}, 命令={}, 错误={}", 
                deviceId, topic, command, e.getMessage());
            // 不抛出异常，避免影响业务逻辑
        }
    }

    /**
     * 锁定/解锁设备命令
     * @param deviceId 设备ID
     * @param lockStatus 锁定状态：0-解锁，1-锁定
     */
    public void sendLockCommand(String deviceId, Integer lockStatus) {
        String command = "lock " + (lockStatus == null ? 0 : lockStatus);
        sendCommand(deviceId, command);
    }

    /**
     * 灯光控制命令
     * @param deviceId 设备ID
     * @param lightStatus 灯光状态：0-关闭，1-开启
     */
    public void sendLightCommand(String deviceId, Integer lightStatus) {
        String command = "light " + (lightStatus == null ? 0 : lightStatus);
        sendCommand(deviceId, command);
    }

    /**
     * 风扇控制命令
     * @param deviceId 设备ID
     * @param fanStatus 风扇状态：0-低速，1-高速
     */
    public void sendFanCommand(String deviceId, Integer fanStatus) {
        String command = "fan " + (fanStatus == null ? 0 : fanStatus);
        sendCommand(deviceId, command);
    }
}