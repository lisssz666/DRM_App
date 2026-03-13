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
    private static final String TOPIC_SUFFIX_STATUS = "/status";
    private static final String TOPIC_SUFFIX_CTR = "/ctr";
    private static final String TOPIC_SUFFIX_HEART = "/heart";

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

            // 解析设备ID和消息类型
            TopicInfo topicInfo = parseTopicInfo(topic);
            if (topicInfo == null) {
                logger.warn("无法从主题中解析设备ID和类型: {}", topic);
                return;
            }

            String deviceId = topicInfo.getDeviceId();
            String topicType = topicInfo.getTopicType();

            // 根据消息类型处理
            switch (topicType) {
                case "status":
                    processStatusMessage(deviceId, messageContent);
                    break;
                case "ctr":
                    processControlMessage(deviceId, messageContent);
                    break;
                case "heart":
                    processHeartbeatMessage(deviceId, messageContent);
                    break;
                default:
                    logger.warn("未知主题类型: {}", topicType);
                    break;
            }

        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从主题中解析设备ID和类型
     * 主题格式: spray/{deviceId}/{type}
     */
    private TopicInfo parseTopicInfo(String topic) {
        if (topic.startsWith(TOPIC_PREFIX)) {
            String[] parts = topic.substring(TOPIC_PREFIX.length()).split("/");
            if (parts.length == 2) {
                return new TopicInfo(parts[0], parts[1]);
            }
        }
        return null;
    }

    /**
     * 主题信息内部类
     */
    private static class TopicInfo {
        private String deviceId;
        private String topicType;

        public TopicInfo(String deviceId, String topicType) {
            this.deviceId = deviceId;
            this.topicType = topicType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getTopicType() {
            return topicType;
        }
    }

    /**
     * 处理状态消息（旧格式）
     * 消息格式示例:
     * - oil_low 1 (油量不足提醒)
     * - pump_usage 120 (气泵使用时间，单位分钟)
     * - posture 0 (设备状态：0-竖立，1-倾倒)
     * - liquid_level 0 (液位指示：0-低，1-中，2-高)
     */
    private void processStatusMessage(String deviceId, String messageContent) {
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
     * 处理设备控制消息
     * 消息格式: 机器设备号 20Byte + 设备状态信息
     */
    private void processControlMessage(String deviceId, String messageContent) {
        logger.info("开始处理设备控制消息: 设备ID={}, 消息内容={}", deviceId, messageContent);
        
        // 查找设备
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            logger.warn("设备不存在: {}", deviceId);
            return;
        }

        try {
            // 解析消息内容（20字节设备号 + 状态信息）
            if (messageContent.length() < 20) {
                logger.warn("控制消息格式错误: 长度不足20字节，内容={}, 长度={}", messageContent, messageContent.length());
                return;
            }

            // 提取设备号（前20字节）
            String deviceNumber = messageContent.substring(0, 20).trim();
            // 提取状态信息（20字节之后）
            String statusInfo = messageContent.substring(20).trim();

            logger.info("设备{}控制消息: 设备号={}, 状态信息={}, 状态信息长度={}", deviceId, deviceNumber, statusInfo, statusInfo.length());

            // 解析状态信息（根据实际协议格式解析）
            // 这里需要根据具体的状态信息格式进行解析
            // 示例：假设状态信息为16进制字符串，包含设备开关、液位、风扇等状态
            if (!statusInfo.isEmpty()) {
                // 解析状态信息
                parseControlStatus(device, statusInfo);
            } else {
                logger.warn("设备{}控制消息状态信息为空", deviceId);
            }

            // 更新设备信息
            device.setUpdatedTime(new Date());
            deviceRepository.save(device);
            logger.info("设备{}控制信息更新成功", deviceId);

        } catch (Exception e) {
            logger.error("处理设备控制消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理设备心跳消息
     * 消息格式: 机器设备号 20Byte
     */
    private void processHeartbeatMessage(String deviceId, String messageContent) {
        // 查找设备
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            logger.warn("设备不存在: {}", deviceId);
            return;
        }

        try {
            // 解析消息内容（20字节设备号）
            if (messageContent.length() < 20) {
                logger.warn("心跳消息格式错误: 长度不足20字节，内容={}", messageContent);
                return;
            }

            // 提取设备号（前20字节）
            String deviceNumber = messageContent.substring(0, 20).trim();

            logger.info("设备{}心跳消息: 设备号={}, 状态=在线", deviceId, deviceNumber);

            // 更新设备在线状态
            device.setUpdatedTime(new Date());
            deviceRepository.save(device);
            logger.info("设备{}心跳信息更新成功", deviceId);

        } catch (Exception e) {
            logger.error("处理设备心跳消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析控制状态信息
     * @param device 设备对象
     * @param statusInfo 状态信息
     */
    private void parseControlStatus(Device device, String statusInfo) {
        try {
            // 这里根据实际的状态信息格式进行解析
            // 示例：假设statusInfo是16进制字符串，每两位表示一个状态值
            // 例如：B0 B1 B2 B3... 其中B0表示设备开关，B1表示液位，B2表示风扇状态等
            
            // 简单示例实现，实际需要根据具体协议调整
            if (statusInfo.length() >= 2) {
                // 解析设备开关状态（假设第一个字节）
                String powerStatusHex = statusInfo.substring(0, 2);
                int powerStatus = Integer.parseInt(powerStatusHex, 16);
                device.setDeviceStatus(powerStatus == 1);
                logger.info("设备开关状态: {}", powerStatus == 1 ? "开启" : "关闭");
            }

            if (statusInfo.length() >= 4) {
                // 解析液位状态（假设第二个字节）
                String liquidLevelHex = statusInfo.substring(2, 4);
                int liquidLevel = Integer.parseInt(liquidLevelHex, 16);
                device.setLiquidLevel(liquidLevel);
                logger.info("设备液位状态: {}", getLiquidLevelDescription(liquidLevel));
            }

            if (statusInfo.length() >= 6) {
                // 解析风扇状态（假设第三个字节）
                String fanStatusHex = statusInfo.substring(4, 6);
                int fanStatus = Integer.parseInt(fanStatusHex, 16);
                device.setFanStatus(fanStatus == 1);
                logger.info("设备风扇状态: {}", fanStatus == 1 ? "开启" : "关闭");
            }

            // 可以根据需要解析更多状态信息

        } catch (Exception e) {
            logger.error("解析控制状态信息失败: {}", e.getMessage(), e);
        }
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
