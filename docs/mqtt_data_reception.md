# 香薰机MQTT数据接收功能文档

## 1. 功能概述

本功能实现了香薰机通过MQTT协议向应用发送数据的能力，主要包括以下特性：

- **油量不足提醒**：当香薰机油量接近设定值时，接收油量不足提醒
- **气泵更换提醒**：当气泵使用时间达到2小时时，提醒用户更换气泵
- **设备状态接收**：接收设备的竖立/倾倒状态和液位指示信息
- **实时数据处理**：通过MQTT消息实时处理设备数据并存储到数据库
- **提供REST API**：支持查询设备状态信息和重置气泵使用时间

## 2. 技术实现

### 2.1 核心组件

#### MQTTMessageHandlerService
- **作用**：处理从香薰机接收到的MQTT消息
- **路径**：`src/main/java/com/cgnpc/drm/service/MQTTMessageHandlerService.java`
- **功能**：
  - 解析MQTT消息主题和内容
  - 处理油量不足、气泵使用时间、设备状态和液位指示信息
  - 更新设备信息到数据库

#### MQTTConfig
- **作用**：配置MQTT客户端和消息订阅
- **路径**：`src/main/java/com/cgnpc/drm/config/MQTTConfig.java`
- **功能**：
  - 创建和初始化MQTT客户端
  - 订阅设备状态主题 `spray/+/status`
  - 设置MQTT消息回调，接收消息并转发给MQTTMessageHandlerService处理

#### Device实体类
- **作用**：存储设备状态信息
- **路径**：`src/main/java/com/cgnpc/drm/entity/Device.java`
- **新增字段**：
  - `pumpUsageTime`：气泵使用时间（分钟）
  - `devicePosture`：设备状态（0-竖立，1-倾倒）
  - `liquidLevel`：液位指示（0-低，1-中，2-高）
  - `oilLowAlert`：油量不足提醒（true-提醒，false-正常）
  - `pumpReplaceAlert`：气泵更换提醒（true-提醒，false-正常）
  - `lastPumpResetTime`：上次重置气泵使用时间的时间

### 2.2 MQTT消息处理流程

1. **消息订阅**：应用启动时，MQTT客户端自动订阅 `spray/+/status` 主题
2. **消息接收**：香薰机发送消息到对应主题，MQTT客户端接收消息
3. **消息处理**：MQTTMessageHandlerService解析消息内容，根据消息类型处理不同的业务逻辑
4. **数据存储**：处理完成后，将设备状态信息更新到数据库
5. **状态查询**：前端通过REST API查询设备状态信息

## 3. 消息格式

### 3.1 主题格式
```
spray/{deviceId}/status
```
- `{deviceId}`：设备唯一标识，如 `P205`

### 3.2 消息内容格式

| 消息类型 | 格式 | 描述 |
|---------|------|------|
| 油量不足提醒 | `oil_low 1` | 1表示油量不足，0表示正常 |
| 气泵使用时间 | `pump_usage 120` | 气泵使用时间，单位分钟 |
| 设备状态 | `posture 0` | 0表示竖立，1表示倾倒 |
| 液位指示 | `liquid_level 0` | 0表示低，1表示中，2表示高 |

## 4. REST API

### 4.1 获取设备状态信息
- **端点**：`GET /api/device/getDeviceStatusInfo`
- **参数**：`deviceId`（设备ID）
- **返回**：设备完整状态信息，包括油量、气泵使用时间、设备状态等

### 4.2 重置气泵使用时间
- **端点**：`PUT /api/device/resetPumpUsageTime`
- **参数**：`deviceId`（设备ID）
- **返回**：更新后的设备信息

## 5. 代码示例

### 5.1 MQTT消息处理示例

```java
// 处理接收到的MQTT消息
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
```

### 5.2 消息内容处理示例

```java
// 处理消息内容
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

        // 其他消息类型处理...
    }

    // 更新设备信息
    device.setUpdatedTime(new Date());
    deviceRepository.save(device);
    logger.info("设备{}信息更新成功", deviceId);
}
```

## 6. 配置说明

### 6.1 MQTT配置

在 `application.yml` 文件中配置MQTT连接信息：

```yaml
mqtt:
  broker-url: tcp://localhost:1883  # MQTT broker地址
  client-id: drm-sprayer-app        # 客户端ID
  username: admin                   # MQTT用户名（可选）
  password: password                # MQTT密码（可选）
  qos: 1                            # 服务质量等级
  keep-alive-interval: 60           # 心跳间隔（秒）
```

### 6.2 依赖配置

在 `pom.xml` 文件中添加MQTT客户端依赖：

```xml
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.5</version>
</dependency>
```

## 7. 测试方法

1. **启动MQTT Broker**：确保MQTT Broker（如Mosquitto）正在运行
2. **启动应用**：运行Spring Boot应用
3. **发送测试消息**：使用MQTT客户端工具（如MQTT.fx）向 `spray/{deviceId}/status` 主题发送测试消息
4. **查看日志**：应用日志中会显示接收到的MQTT消息
5. **查询设备状态**：通过 `GET /api/device/getDeviceStatusInfo` 端点查询设备状态，确认数据已更新

## 8. 注意事项

1. **设备ID一致性**：确保香薰机发送消息时使用的设备ID与应用中注册的设备ID一致
2. **消息格式正确**：香薰机发送的消息必须符合指定的格式，否则应用可能无法正确处理
3. **网络连接**：确保香薰机与MQTT Broker之间的网络连接稳定
4. **数据存储**：设备状态信息会存储到数据库中，确保数据库连接正常
5. **气泵使用时间**：当气泵使用时间达到2小时时，系统会自动提醒更换气泵，用户可通过 `PUT /api/device/resetPumpUsageTime` 端点重置使用时间

## 9. 总结

本功能实现了香薰机与应用之间的实时数据通信，通过MQTT协议接收设备状态信息，并提供了完整的REST API供前端查询和操作。系统能够及时提醒用户香薰机油量不足和气泵需要更换的情况，提高了设备的智能化程度和用户体验。
