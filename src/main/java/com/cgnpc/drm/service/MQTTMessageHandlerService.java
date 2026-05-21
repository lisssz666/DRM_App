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
    private static final String TOPIC_SUFFIX_INFO = "/info"; // 新增主题 - 接收硬件传的设备信息

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 处理接收到的MQTT消息
     * @param topic 消息主题
     * @param message 消息内容
     */
    public void handleMessage(String topic, MqttMessage message) {
        try {
            byte[] payload = message.getPayload();
            String messageContent = new String(payload);
            
            // 打印原始数据信息，方便测试调试
            logger.info("========== MQTT消息接收开始 ==========");
            logger.info("主题: {}", topic);
            logger.info("消息长度: {} 字节", payload.length);
            logger.info("消息内容(字符串): {}", messageContent);
            logger.info("消息内容(十六进制): {}", bytesToHex(payload));
            logger.info("QoS: {}", message.getQos());
            logger.info("是否保留消息: {}", message.isRetained());
            logger.info("========== MQTT消息接收结束 ==========");

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
                    processStatusMessage(deviceId, messageContent); // 处理旧格式状态消息
                    break;
                case "ctr":
                    processControlMessage(deviceId, messageContent); // 处理控制消息
                                       break;
                case "heart":
                    processHeartbeatMessage(deviceId, messageContent); // 处理心跳消息
                    break;
                case "info":
                    processDeviceInfoMessage(deviceId, messageContent);
                    break;
                case "alarm":
                    processAlarmMessage(deviceId, messageContent);
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

    /**
     * 处理设备配网信息消息
     * 消息格式: 0xcd + 机器设备号(20Byte) + MAC地址(蓝牙+WIFI) + 固件版本 + 软件版本 + 联网 + 机型 + 机型 + 累加和
     * @param deviceId 设备ID
     * @param messageContent 消息内容（16进制字符串）
     */
    private void processDeviceInfoMessage(String deviceId, String messageContent) {
        logger.info("开始处理设备配网信息消息: 设备ID={}, 消息内容={}", deviceId, messageContent);

        try {
            // 解析16进制消息
            byte[] data = hexStringToByteArray(messageContent);
            if (data == null || data.length < 35) {
                logger.warn("配网信息消息格式错误: 长度不足35字节，内容={}, 长度={}", messageContent, data != null ? data.length : 0);
                return;
            }

            // 验证起始字节 0xcd
            if (data[0] != (byte) 0xcd) {
                logger.warn("配网信息消息起始字节错误: 期望0xcd, 实际0x{}", String.format("%02X", data[0]));
                return;
            }

            // 解析设备号 (B1-B20)
            String deviceNumber = new String(data, 1, 20, "UTF-8").trim();
            logger.info("设备号: {}", deviceNumber);

            // 解析MAC地址 (B21-B26) - 蓝牙+WIFI
            StringBuilder macAddress = new StringBuilder();
            for (int i = 21; i <= 26; i++) {
                if (i > 21) macAddress.append(":");
                macAddress.append(String.format("%02X", data[i]));
            }
            String mac = macAddress.toString();
            logger.info("MAC地址: {}", mac);

            // 解析固件版本 (B27)
            String firmwareVersion = String.format("%d.%d", (data[27] >> 4) & 0x0F, data[27] & 0x0F);
            logger.info("固件版本: {}", firmwareVersion);

            // 解析软件版本 (B28)
            String softwareVersion = String.format("%d.%d", (data[28] >> 4) & 0x0F, data[28] & 0x0F);
            logger.info("软件版本: {}", softwareVersion);

            // 解析联网状态 (B29)
            boolean networkStatus = data[29] == 1;
            logger.info("联网状态: {}", networkStatus ? "已联网" : "未联网");

            // 解析机型 (B30-B32)
            String deviceType = parseDeviceType(data[30], data[31], data[32]);
            logger.info("机型: {}", deviceType);

            // 验证累加和 (B34)
            byte checksum = calculateChecksum(data, 0, 33);
            if (checksum != data[34]) {
                logger.warn("配网信息消息校验和错误: 期望0x{}, 实际0x{}", 
                    String.format("%02X", checksum), String.format("%02X", data[34]));
                return;
            }

            // 检查设备是否已存在
            Device device = deviceRepository.findByDeviceId(deviceNumber);
            if (device == null) {
                // 创建设备
                device = new Device();
                device.setDeviceId(deviceNumber);
                device.setDeviceName("香薰机" + deviceNumber); // 默认设备名称
                device.setModel(generateDeviceModel()); // 生成设备型号
                device.setUserId(1L); // 默认用户ID，后续可以根据业务调整
                device.setEssentialOilName("默认精油"); // 默认精油名称
                device.setEssentialOilLevel(100); // 默认精油量100%
                device.setFanStatus(false);
                device.setDeviceStatus(false);
                device.setLockStatus(false);
                device.setLightStatus(false);
                device.setFanSpeed(50);
                device.setCreatedTime(new Date());
                logger.info("创建新设备: {}", deviceNumber);
            } else {
                logger.info("更新现有设备: {}", deviceNumber);
            }

            // 更新配网信息
            device.setMacAddress(mac);
            device.setFirmwareVersion(firmwareVersion);
            device.setSoftwareVersion(softwareVersion);
            device.setNetworkStatus(networkStatus);
            device.setDeviceType(deviceType);
            device.setUpdatedTime(new Date());

            // 保存设备
            deviceRepository.save(device);
            logger.info("设备{}配网信息处理成功", deviceNumber);

        } catch (Exception e) {
            logger.error("处理设备配网信息消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 将16进制字符串转换为字节数组
     * @param hexString 16进制字符串
     * @return 字节数组
     */
    private byte[] hexStringToByteArray(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            return null;
        }
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 计算累加和校验
     * @param data 数据
     * @param start 起始位置
     * @param end 结束位置
     * @return 累加和
     */
    private byte calculateChecksum(byte[] data, int start, int end) {
        int sum = 0;
        for (int i = start; i <= end && i < data.length; i++) {
            sum += data[i] & 0xFF;
        }
        return (byte) (sum & 0xFF);
    }

    /**
     * 解析机型
     * @param b30 B30字节
     * @param b31 B31字节
     * @param b32 B32字节
     * @return 机型描述
     */
    private String parseDeviceType(byte b30, byte b31, byte b32) {
        // 根据实际协议解析机型
        // 这里假设B30表示WIFI支持，B31表示蓝牙支持，B32表示其他特性
        boolean hasWifi = (b30 & 0x01) != 0;
        boolean hasBluetooth = (b31 & 0x01) != 0;

        if (hasWifi && hasBluetooth) {
            return "wifi_bluetooth";
        } else if (hasWifi) {
            return "wifi";
        } else if (hasBluetooth) {
            return "bluetooth";
        } else {
            return "unknown";
        }
    }

    /**
     * 生成设备型号：大写字母+3个随机数字
     */
    private String generateDeviceModel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            char c = (char) (65 + (int) (Math.random() * 26)); // 65是'A'的ASCII码
            sb.append(c);
        }
        for (int i = 0; i < 3; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * 处理设备报警消息
     * 消息格式: 机器设备号(20Byte) + 年 + 月 + 日 + 时 + 分 + 秒 + 错误代码1 + 错误代码2 + 校验码
     * 总长度: 31字节
     * @param deviceId 设备ID
     * @param messageContent 消息内容（原始字符串或16进制字符串）
     */
    private void processAlarmMessage(String deviceId, String messageContent) {
        logger.info("开始处理设备报警消息: 设备ID={}, 消息内容(原始)={}", deviceId, messageContent);

        try {
            // 清理消息内容：去除引号、空格等干扰字符
            String cleanedContent = messageContent.trim().replace("\"", "");
            // 尝试解析为16进制数据
            byte[] data = hexStringToByteArray(cleanedContent);
            // 如果16进制解析失败，直接使用原始字节
            if (data == null || data.length == 0) {
                data = cleanedContent.getBytes();
            }
            
            logger.info("报警消息数据长度: {} 字节", data.length);
            logger.info("报警消息十六进制: {}", bytesToHex(data));
            
            // 最小长度检查：至少需要20字节的设备号
            if (data == null || data.length < 20) {
                logger.warn("报警消息格式错误: 长度不足20字节（最小设备号长度），数据长度={}", data != null ? data.length : 0);
                return;
            }

            // 解析设备号 (B0-B19) - 共20字节
            String deviceNumber = new String(data, 0, 20, "UTF-8").trim();
            logger.info("报警设备号: {}", deviceNumber);

            // 完整消息需要31字节，检查是否有完整数据
            if (data.length < 31) {
                logger.warn("报警消息数据不完整: 期望31字节，实际{}字节。可能是部分消息或数据截断。", data.length);
                
                // 尝试使用主题中的设备ID查找设备
                Device device = deviceRepository.findByDeviceId(deviceId);
                if (device != null) {
                    logger.info("使用主题中的设备ID({})查找设备成功", deviceId);
                    deviceNumber = deviceId;
                } else {
                    // 使用解析出的设备号查找
                    device = deviceRepository.findByDeviceId(deviceNumber);
                }
                
                if (device != null) {
                    logger.warn("数据不完整，跳过时间和错误代码解析，仅记录报警事件");
                    device.setUpdatedTime(new Date());
                    deviceRepository.save(device);
                }
                return;
            }

            // 解析时间信息
            int year = 2000 + (data[20] & 0xFF); // B21: 年份（相对于2000）
            int month = data[21] & 0xFF; // B22: 月份
            int day = data[22] & 0xFF; // B23: 日
            int hour = data[23] & 0xFF; // B24: 时
            int minute = data[24] & 0xFF; // B25: 分
            int second = data[25] & 0xFF; // B26: 秒

            logger.info("报警时间: {}-{}-{} {}:{}:{}", year, month, day, hour, minute, second);
            // 解析错误代码
            byte errorCode1 = data[26]; // B27: 错误代码1
            byte errorCode2 = data[27]; // B28: 错误代码2
            
            logger.info("错误代码1: 0x{} ({})", String.format("%02X", errorCode1), getErrorCodeDescription(errorCode1));
            logger.info("错误代码2: 0x{} ({})", String.format("%02X", errorCode2), getErrorCodeDescription(errorCode2));
            // 验证校验码 (B30)
            byte checksum = calculateChecksum(data, 0, 29);
            if (checksum != data[30]) {
                logger.warn("报警消息校验和错误: 期望0x{}, 实际0x{}", 
                    String.format("%02X", checksum), String.format("%02X", data[30]));
                // 校验和错误但仍尝试处理，记录警告
            }
            // 解析错误代码并更新设备状态
            Device device = deviceRepository.findByDeviceId(deviceNumber);
            if (device == null) {
                logger.warn("设备不存在: {}", deviceNumber);
                return;
            }
            // 处理错误代码1
            processErrorCode(device, errorCode1);
            // 处理错误代码2
            processErrorCode(device, errorCode2);
            // 更新设备信息
            device.setUpdatedTime(new Date());
            deviceRepository.save(device);
            logger.info("设备{}报警信息处理成功", deviceNumber);
        } catch (Exception e) {
            logger.error("处理设备报警消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理错误代码
     * @param device 设备对象
     * @param errorCode 错误代码
     */
    private void processErrorCode(Device device, byte errorCode) {
        String errorDesc = getErrorCodeDescription(errorCode);
        logger.info("处理错误代码 0x{}: {}", String.format("%02X", errorCode), errorDesc);

        switch (errorCode & 0xFF) {
            case 0x80: // 喷香机倾斜
                device.setDevicePosture(1); // 1-倾倒
                break;
            case 0x81: // 低油液报警
                device.setOilLowAlert(true);
                device.setLiquidLevel(0); // 低液位
                break;
            case 0x82: // 油耗尽报警
                device.setOilLowAlert(true);
                device.setLiquidLevel(0); // 低液位
                device.setEssentialOilLevel(0);
                break;
            case 0x83: // 气泵损坏
                device.setPumpReplaceAlert(true);
                break;
            case 0x00: // 无错误（占位）
                break;
            default:
                logger.warn("未知错误代码: 0x{}", String.format("%02X", errorCode));
                break;
        }
    }

    /**
     * 获取错误代码描述
     * @param errorCode 错误代码
     * @return 错误描述
     */
    private String getErrorCodeDescription(byte errorCode) {
        switch (errorCode & 0xFF) {
            case 0x80:
                return "喷香机倾斜";
            case 0x81:
                return "低油液报警";
            case 0x82:
                return "油耗尽报警";
            case 0x83:
                return "气泵损坏";
            case 0x00:
                return "无错误";
            default:
                return "未知错误";
        }
    }

    /**
     * 将字节数组转换为十六进制字符串，用于打印原始数据
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
