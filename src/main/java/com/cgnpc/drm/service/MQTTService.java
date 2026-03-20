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
    private static final String TOPIC_SUFFIX = "/info";

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttConnectOptions mqttConnectOptions;

    @Autowired
    private int mqttQos;

    @Autowired
    private String mqttBrokerUrl;

    /**
     * 发送MQTT命令到设备
     * @param deviceId 设备ID
     * @param deviceNumber 机器设备号 (20字节)
     * @param deviceStatus 设备状态信息
     */
    public void sendCommand(String deviceId, String deviceNumber, DeviceStatus deviceStatus) {
        String topic = TOPIC_PREFIX + deviceId + TOPIC_SUFFIX;
        try {
            // 检查并确保MQTT客户端已连接
            if (!mqttClient.isConnected()) {
                logger.warn("MQTT客户端未连接，尝试重新连接，b roker地址: {}", mqttBrokerUrl);
                mqttClient.connect(mqttConnectOptions);
                logger.info("MQTT客户端重新连接成功，broker地址: {}", mqttBrokerUrl);
            }
            
            // 打印发送前的设备状态详细信息
            logger.info("准备发送MQTT命令: 设备ID={}, 主题={}, 设备号={}", deviceId, topic, deviceNumber);
            logger.info("设备状态详情: 电源={}, 液位={}, 定时={}, 风扇档位={}, 童锁={}, 工作状态={}, 锁定状态={}", 
                deviceStatus.getPowerStatus(), deviceStatus.getLiquidLevel(), deviceStatus.getTimerStatus(),
                deviceStatus.getFanSpeed(), deviceStatus.getChildLock(), deviceStatus.getWorkStatus(),
                deviceStatus.getLockStatus());
            
            // 构建发送数据
            byte[] data = buildCommandData(deviceNumber, deviceStatus);
            
            // 打印发送的数据（十六进制格式）
            StringBuilder hexData = new StringBuilder();
            for (byte b : data) {
                hexData.append(String.format("%02X ", b));
            }
            logger.info("发送数据 (十六进制): {}", hexData.toString().trim());
            
            MqttMessage message = new MqttMessage(data);
            message.setQos(mqttQos);
            mqttClient.publish(topic, message);
            logger.info("发送MQTT命令成功: 设备ID={}, 主题={}, 设备号={}, 数据长度={}字节", 
                deviceId, topic, deviceNumber, data.length);
        } catch (MqttException e) {
            logger.error("发送MQTT命令失败: 设备ID={}, 主题={}, 错误={}", 
                deviceId, topic, e.getMessage());
            // 不抛出异常，避免影响业务逻辑
        }
    }

    /**
     * 构建命令数据
     * 数据格式：0xcd 0xef + 机器设备号(20字节) + 设备状态信息 + 累加和校验
     */
    private byte[] buildCommandData(String deviceNumber, DeviceStatus deviceStatus) {
        try {
            // 计算总长度：2(起始字节) + 20(设备号) + 10(状态信息) + 1(校验和) = 33字节
            byte[] data = new byte[33];
            
            // 设置起始字节
            data[0] = (byte) 0xCD;
            data[1] = (byte) 0xEF;
            
            // 设置设备号（20字节，不足补空格）
            byte[] deviceNumberBytes = deviceNumber.getBytes();
            for (int i = 0; i < 20; i++) {
                if (i < deviceNumberBytes.length) {
                    data[2 + i] = deviceNumberBytes[i];
                } else {
                    data[2 + i] = (byte) ' '; // 不足补空格
                }
            }
            
            // 设置设备状态信息（从B23开始）
            int statusOffset = 22; // 2(起始) + 20(设备号) = 22
            
            // B23: 设备开关
            data[statusOffset] = (byte) (deviceStatus.getPowerStatus() ? 1 : 0);
            
            // B24: 液位状态
            data[statusOffset + 1] = (byte) deviceStatus.getLiquidLevel();
            
            // B25: 定时状态
            data[statusOffset + 2] = (byte) deviceStatus.getTimerStatus();
            
            // B26-B27: 保留
            data[statusOffset + 3] = 0;
            data[statusOffset + 4] = 0;
            
            // B28: 风扇档位
            data[statusOffset + 5] = (byte) deviceStatus.getFanSpeed();
            
            // B29: 童锁
            data[statusOffset + 6] = (byte) (deviceStatus.getChildLock() ? 1 : 0);
            
            // B30: 保留
            data[statusOffset + 7] = 0;
            
            // B31: 工作状态
            data[statusOffset + 8] = (byte) deviceStatus.getWorkStatus();
            
            // B32: 机器是否上锁
            data[statusOffset + 9] = (byte) (deviceStatus.getLockStatus() ? 1 : 0);
            
            // 计算累加和校验
            int checksum = 0;
            for (int i = 0; i < 32; i++) {
                checksum += data[i] & 0xFF;
            }
            data[32] = (byte) (checksum & 0xFF);
            
            return data;
        } catch (Exception e) {
            logger.error("构建命令数据失败: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * 锁定/解锁设备命令
     * @param deviceId 设备ID
     * @param deviceNumber 机器设备号
     * @param lockStatus 锁定状态：true-锁定，false-解锁
     */
    public void sendLockCommand(String deviceId, String deviceNumber, boolean lockStatus) {
        DeviceStatus status = new DeviceStatus();
        status.setLockStatus(lockStatus);
        sendCommand(deviceId, deviceNumber, status);
    }

    /**
     * 灯光控制命令
     * @param deviceId 设备ID
     * @param deviceNumber 机器设备号
     * @param lightStatus 灯光状态：true-开启，false-关闭
     */
    public void sendLightCommand(String deviceId, String deviceNumber, boolean lightStatus) {
        DeviceStatus status = new DeviceStatus();
        // 假设灯光状态映射到工作状态
        status.setWorkStatus(lightStatus ? 1 : 0);
        sendCommand(deviceId, deviceNumber, status);
    }

    /**
     * 风扇控制命令
     * @param deviceId 设备ID
     * @param deviceNumber 机器设备号
     * @param fanSpeed 风扇档位：0-低速，1-高速
     */
    public void sendFanCommand(String deviceId, String deviceNumber, int fanSpeed) {
        DeviceStatus status = new DeviceStatus();
        status.setFanSpeed(fanSpeed);
        sendCommand(deviceId, deviceNumber, status);
    }

    /**
     * 设备状态类
     */
    public static class DeviceStatus {
        private boolean powerStatus = false; // 设备开关
        private int liquidLevel = 0; // 液位状态
        private int timerStatus = 0; // 定时状态
        private int fanSpeed = 0; // 风扇档位
        private boolean childLock = false; // 童锁
        private int workStatus = 0; // 工作状态
        private boolean lockStatus = false; // 机器是否上锁

        // Getters and Setters
        public boolean getPowerStatus() {
            return powerStatus;
        }

        public void setPowerStatus(boolean powerStatus) {
            this.powerStatus = powerStatus;
        }

        public int getLiquidLevel() {
            return liquidLevel;
        }

        public void setLiquidLevel(int liquidLevel) {
            this.liquidLevel = liquidLevel;
        }

        public int getTimerStatus() {
            return timerStatus;
        }

        public void setTimerStatus(int timerStatus) {
            this.timerStatus = timerStatus;
        }

        public int getFanSpeed() {
            return fanSpeed;
        }

        public void setFanSpeed(int fanSpeed) {
            this.fanSpeed = fanSpeed;
        }

        public boolean getChildLock() {
            return childLock;
        }

        public void setChildLock(boolean childLock) {
            this.childLock = childLock;
        }

        public int getWorkStatus() {
            return workStatus;
        }

        public void setWorkStatus(int workStatus) {
            this.workStatus = workStatus;
        }

        public boolean getLockStatus() {
            return lockStatus;
        }

        public void setLockStatus(boolean lockStatus) {
            this.lockStatus = lockStatus;
        }
    }
}