package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.repository.DeviceRepository;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * MQTT消息处理服务
 * 用于处理从香薰机接收到的数据
 */
@Service
public class MQTTMessageHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(MQTTMessageHandlerService.class);
    private static final String TOPIC_PREFIX = "spray/";
    private static final String TOPIC_SUFFIX = "/status";

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 处理接收到的MQTT消息
     * @param topic 消息主题
     * @param message 消息内容
     */
    public void handleMessage(String topic, MqttMessage message) {
        try {
            String messageContent = new String(message.getPayload());
            logger.info("接收到MQTT消息: 主题={}, 内容={}", topic, messageContent);

            // 解析设备ID
            String deviceId = parseDeviceIdFromTopic(topic);
            if (deviceId == null) {
                logger.warn("无法从主题中解析设备ID: {}", topic);
                return;
            }

            // 处理消息内容
            processMessage(deviceId, messageContent);

        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从主题中解析设备ID
     * 主题格式: spray/{deviceId}/status
     */
    private String parseDeviceIdFromTopic(String topic) {
        if (topic.startsWith(TOPIC_PREFIX) && topic.endsWith(TOPIC_SUFFIX)) {
            return topic.substring(TOPIC_PREFIX.length(), topic.length() - TOPIC_SUFFIX.length());
        }
        return null;
    }

    /**
     * 处理消息内容
     * 消息格式示例:
     * - oil_low 1 (油量不足提醒)
     * - pump_usage 120 (气泵使用时间，单位分钟)
     * - posture 0 (设备状态：0-竖立，1-倾倒)
     * - liquid_level 0 (液位指示：0-低，1-中，2-高)
     */
    private void processMessage(String deviceId, String messageContent) {
        // 查找设备
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            logger.warn("设备不存在: {}", deviceId);
            return;
        }

        // 解析消息内容
        String[] parts = messageContent.split(" ");
        if (parts.length < 2) {
            logger.warn("消息格式错误: {}", messageContent);
            return;
        }

        String command = parts[0];
        String value = parts[1];

        switch (command) {
            case "oil_low":
                // 油量不足提醒
                boolean oilLow = "1".equals(value);
                device.setOilLowAlert(oilLow);
                if (oilLow) {
                    logger.info("设备{}油量不足提醒", deviceId);
                }
                break;

            case "pump_usage":
                // 气泵使用时间
                try {
                    int usageTime = Integer.parseInt(value);
                    device.setPumpUsageTime(usageTime);
                    // 检查是否需要更换气泵（2小时=120分钟）
                    if (usageTime >= 120) {
                        device.setPumpReplaceAlert(true);
                        logger.info("设备{}气泵使用时间达到2小时，需要更换", deviceId);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("气泵使用时间格式错误: {}", value);
                }
                break;

            case "posture":
                // 设备状态
                try {
                    int posture = Integer.parseInt(value);
                    device.setDevicePosture(posture);
                    logger.info("设备{}状态更新: {}", deviceId, posture == 0 ? "竖立" : "倾倒");
                } catch (NumberFormatException e) {
                    logger.warn("设备状态格式错误: {}", value);
                }
                break;

            case "liquid_level":
                // 液位指示
                try {
                    int liquidLevel = Integer.parseInt(value);
                    device.setLiquidLevel(liquidLevel);
                    logger.info("设备{}液位更新: {}", deviceId, getLiquidLevelDescription(liquidLevel));
                } catch (NumberFormatException e) {
                    logger.warn("液位指示格式错误: {}", value);
                }
                break;

            default:
                logger.warn("未知命令: {}", command);
                break;
        }

        // 更新设备信息
        device.setUpdatedTime(new Date());
        deviceRepository.save(device);
        logger.info("设备{}信息更新成功", deviceId);
    }

    /**
     * 获取液位描述
     */
    private String getLiquidLevelDescription(int liquidLevel) {
        switch (liquidLevel) {
            case 0:
                return "低";
            case 1:
                return "中";
            case 2:
                return "高";
            default:
                return "未知";
        }
    }

    /**
     * 重置气泵使用时间
     * @param deviceId 设备ID
     */
    public void resetPumpUsageTime(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device != null) {
            device.setPumpUsageTime(0);
            device.setPumpReplaceAlert(false);
            device.setLastPumpResetTime(new Date());
            device.setUpdatedTime(new Date());
            deviceRepository.save(device);
            logger.info("设备{}气泵使用时间已重置", deviceId);
        }
    }
}
