package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.entity.WorkingMode;
import com.cgnpc.drm.repository.DeviceRepository;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.service.MQTTService;
import com.cgnpc.drm.service.MQTTMessageHandlerService;
import com.cgnpc.drm.service.WorkingModeService;
import com.cgnpc.drm.service.GroupService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MQTTService mqttService;

    @Autowired
    private MQTTMessageHandlerService mqttMessageHandlerService;

    @Autowired
    private WorkingModeService workingModeService;

    @Autowired
    private GroupService groupService;

    @Override
    public Device getDeviceInfo(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public Device controlDevice(String deviceId, DeviceControlDTO controlDTO) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        // 创建设备状态对象
        MQTTService.DeviceStatus deviceStatus = new MQTTService.DeviceStatus();

        // 更新设备状态并设置到deviceStatus
        if (controlDTO.getFanStatus() != null) {
            device.setFanStatus(controlDTO.getFanStatus());
        }
        if (controlDTO.getDeviceStatus() != null) {
            device.setDeviceStatus(controlDTO.getDeviceStatus());
            deviceStatus.setPowerStatus(controlDTO.getDeviceStatus());
        }
        if (controlDTO.getLockStatus() != null) {
            device.setLockStatus(controlDTO.getLockStatus());
            deviceStatus.setLockStatus(controlDTO.getLockStatus());
        }
        if (controlDTO.getLightStatus() != null) {
            device.setLightStatus(controlDTO.getLightStatus());
            // 假设灯光状态映射到工作状态
            deviceStatus.setWorkStatus(controlDTO.getLightStatus() ? 1 : 0);
        }
        if (controlDTO.getFanSpeed() != null) {
            device.setFanSpeed(controlDTO.getFanSpeed());
            deviceStatus.setFanSpeed(controlDTO.getFanSpeed());
        }
        if (controlDTO.getEssentialOilName() != null) {
            device.setEssentialOilName(controlDTO.getEssentialOilName());
        }
        if (controlDTO.getEssentialOilLevel() != null) {
            device.setEssentialOilLevel(controlDTO.getEssentialOilLevel());
        }
        if (controlDTO.getCurrentModeId() != null) {
            device.setCurrentModeId(controlDTO.getCurrentModeId());
        }
        if (controlDTO.getLiquidLevel() != null) {
            device.setLiquidLevel(controlDTO.getLiquidLevel());
            deviceStatus.setLiquidLevel(controlDTO.getLiquidLevel());
        }
        if (controlDTO.getTimerStatus() != null) {
            deviceStatus.setTimerStatus(controlDTO.getTimerStatus());
        }
        if (controlDTO.getChildLock() != null) {
            deviceStatus.setChildLock(controlDTO.getChildLock());
        }
        if (controlDTO.getWorkStatus() != null) {
            deviceStatus.setWorkStatus(controlDTO.getWorkStatus());
        }

        // 发送完整的控制命令
        mqttService.sendCommand(deviceId, device.getDeviceId(), deviceStatus);

        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device updateDeviceInfo(String deviceId, String deviceName, String essentialOilName) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        // 更新设备名称（如果提供）
        if (deviceName != null && !deviceName.isEmpty()) {
            device.setDeviceName(deviceName);
        }
        
        // 更新精油名称（如果提供）
        if (essentialOilName != null) {
            device.setEssentialOilName(essentialOilName);
        }
        
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }


    // 生成12位字母数字组合的唯一设备ID
    private String generateUniqueDeviceId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder deviceId = new StringBuilder(12);
        
        for (int i = 0; i < 12; i++) {
            deviceId.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        // 检查生成的设备ID是否已存在，若存在则重新生成（最多尝试5次）
        for (int attempt = 0; attempt < 5; attempt++) {
            if (deviceRepository.findByDeviceId(deviceId.toString()) == null) {
                return deviceId.toString();
            }
            // 重新生成
            deviceId = new StringBuilder(12);
            for (int i = 0; i < 12; i++) {
                deviceId.append(characters.charAt(random.nextInt(characters.length())));
            }
            
            // 可选：如果多次尝试失败，增加一点延迟避免无限循环
            if (attempt >= 3) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // 如果尝试多次仍失败，抛出异常
        throw new RuntimeException("Unable to generate unique device ID, please try again later.");  // 无法生成唯一设备ID，请稍后重试
    }

    @Override
    public Device addDevice(Device device) {
        // 自动生成设备ID
        String uniqueDeviceId = generateUniqueDeviceId();
        device.setDeviceId(uniqueDeviceId);
        
        // 设置创建时间和更新时间
        Date now = new Date();
        device.setCreatedTime(now);
        device.setUpdatedTime(now);
        
        // 设置默认值
        if (device.getFanStatus() == null) {
            device.setFanStatus(false);
        }
        if (device.getDeviceStatus() == null) {
            device.setDeviceStatus(false);
        }
        if (device.getLockStatus() == null) {
            device.setLockStatus(false);
        }
        if (device.getLightStatus() == null) {
            device.setLightStatus(false);
        }
        if (device.getFanSpeed() == null) {
            device.setFanSpeed(0);
        }
        if (device.getEssentialOilLevel() == null) {
            device.setEssentialOilLevel(100);
        }
        
        return deviceRepository.save(device);
    }
    
    @Override
    public boolean deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }
        
        deviceRepository.delete(device);
        return true;
    }
    
    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public List<Device> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public boolean validateDeviceOwnership(String deviceId, Long userId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        return device != null && device.getUserId().equals(userId);
    }

    @Override
    public Device resetDeviceToFactorySettings(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        // 重置设备设置为默认值
        device.setEssentialOilName(null);
        device.setEssentialOilLevel(null);
        device.setFanSpeed(0);
        device.setFanStatus(false);
        device.setDeviceStatus(false);
        device.setLockStatus(false);
        device.setLightStatus(false);
        device.setCurrentModeId(null);
        device.setPumpUsageTime(0);
        device.setDevicePosture(null);
        device.setLiquidLevel(0);
        device.setOilLowAlert(false);
        device.setPumpReplaceAlert(false);
        device.setLastPumpResetTime(null);
        device.setUpdatedTime(new Date());

        // 保存设备信息
        device = deviceRepository.save(device);

        // 发送MQTT命令通知设备恢复出厂设置
        MQTTService.DeviceStatus deviceStatus = new MQTTService.DeviceStatus();
        deviceStatus.setPowerStatus(false);
        deviceStatus.setFanSpeed(0);
        deviceStatus.setLockStatus(false);
        deviceStatus.setLiquidLevel(0);
        deviceStatus.setTimerStatus(0);
        deviceStatus.setChildLock(false);
        deviceStatus.setWorkStatus(0);
        mqttService.sendCommand(deviceId, device.getDeviceId(), deviceStatus);

        return device;
    }

    @Override
    public Device resetPumpUsageTime(String deviceId) {
        // 调用MQTTMessageHandlerService重置气泵使用时间
        mqttMessageHandlerService.resetPumpUsageTime(deviceId);
        // 返回更新后的设备信息
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public Map<String, Object> getDeviceStatusInfo(String deviceId) {
        // 使用Optional避免空指针异常
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        // 使用LinkedHashMap保持插入顺序，提高可读性
        Map<String, Object> statusInfo = new java.util.LinkedHashMap<>();
        
        // 构建设备状态信息
        statusInfo.put("deviceId", device.getDeviceId());
        statusInfo.put("deviceName", device.getDeviceName());
        statusInfo.put("model", device.getModel()); // 设备型号
        statusInfo.put("essentialOilName", device.getEssentialOilName());
        statusInfo.put("essentialOilLevel", device.getEssentialOilLevel());
        statusInfo.put("fanStatus", device.getFanStatus());
        statusInfo.put("deviceStatus", device.getDeviceStatus());
        statusInfo.put("lockStatus", device.getLockStatus());
        statusInfo.put("lightStatus", device.getLightStatus());
        statusInfo.put("fanSpeed", device.getFanSpeed());
        statusInfo.put("currentModeId", device.getCurrentModeId());
        statusInfo.put("pumpUsageTime", device.getPumpUsageTime());
        statusInfo.put("devicePosture", device.getDevicePosture());
        statusInfo.put("liquidLevel", device.getLiquidLevel());
        statusInfo.put("oilLowAlert", device.getOilLowAlert());
        statusInfo.put("pumpReplaceAlert", device.getPumpReplaceAlert());
        statusInfo.put("lastPumpResetTime", device.getLastPumpResetTime());
        statusInfo.put("updatedTime", device.getUpdatedTime());
        statusInfo.put("devicePicture", device.getDevicePicture()); // 设备图片

        // 添加状态描述
        statusInfo.put("devicePostureDesc", device.getDevicePosture() == null ? "Unknown" : (device.getDevicePosture() == 0 ? "Upright" : "Tilted"));  // 竖立 : 倾倒
        statusInfo.put("liquidLevelDesc", getLiquidLevelDescription(device.getLiquidLevel()));

        com.cgnpc.drm.entity.Group deviceGroup = groupService.getDeviceGroup(deviceId, device.getUserId());
        statusInfo.put("groupName", deviceGroup != null ? deviceGroup.getGroupName() : "All");

        // 添加启用的工作模式信息
        List<WorkingMode> enabledModes = workingModeService.getEnabledWorkingModes(deviceId);
        
        // 如果没有启用的工作模式，初始化默认模式
        if (enabledModes == null || enabledModes.isEmpty()) {
            WorkingMode defaultMode = new WorkingMode();
            defaultMode.setDeviceId(deviceId);
            defaultMode.setModeName("默认模式");
            defaultMode.setWeekDays("1,2,3,4,5"); // 周一~周五
            defaultMode.setStartTime("00:00");
            defaultMode.setEndTime("23:59");
            defaultMode.setRunTime(15); // 工作15秒
            defaultMode.setStopTime(120); // 暂停120秒
            defaultMode.setIsDefault(true);
            defaultMode.setStatus(true);
            enabledModes = new java.util.ArrayList<>(1); // 预分配容量
            enabledModes.add(defaultMode);
        } else if (enabledModes.size() > 1) {
            // 如果有多个启用的模式，只返回按startTime倒序排序的第一个模式
            enabledModes = new java.util.ArrayList<>(1);
            enabledModes.add(workingModeService.getEnabledWorkingModes(deviceId).get(0));
        }
        
        statusInfo.put("enabledWorkingModes", enabledModes);

        return statusInfo;
    }

    /**
     * 获取液位描述
     */
    private String getLiquidLevelDescription(Integer liquidLevel) {
        if (liquidLevel == null) {
            return "Unknown";  // 未知
        }
        switch (liquidLevel) {
            case 0:
                return "Low";  // 低
            case 1:
                return "Medium";  // 中
            case 2:
                return "High";  // 高
            default:
                return "Unknown";  // 未知
        }
    }

    @Override
    public List<Map<String, Object>> getGroupDevicesInfo(String deviceId, Long userId, boolean includeDevices) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        Map<String, Object> allGroupInfo = null;
        
        // 获取所有分组（包括默认的All分组）
        List<com.cgnpc.drm.entity.Group> groups = groupService.getGroupsByUserId(userId);
        
        // 一次循环处理所有分组
        for (com.cgnpc.drm.entity.Group group : groups) {
            Map<String, Object> groupInfo = new java.util.LinkedHashMap<>();
            
            // 分组基本信息
            groupInfo.put("id", group.getId());
            groupInfo.put("groupName", group.getGroupName());
            
            // 获取该分组下的设备ID列表
            List<String> deviceIds = groupService.getDevicesInGroup(group.getId(), userId);
            int deviceNum = deviceIds.size();
            groupInfo.put("deviceNum", deviceNum);
            
            // 检查是否包含指定的设备
            boolean isSelect = false;
            if (deviceId != null && !deviceId.isEmpty()) {
                isSelect = deviceIds.contains(deviceId);
            }
            groupInfo.put("isSelect", isSelect);
            
            // 如果需要包含设备详情
            if (includeDevices) {
                // 获取设备详细信息（含模式）
                List<Map<String, Object>> devices = new java.util.ArrayList<>();
                for (String devId : deviceIds) {
                    try {
                        Map<String, Object> deviceStatus = getDeviceStatusInfo(devId);
                        // 如果是All分组，获取设备实际所属的分组名称
                        if ("All".equals(group.getGroupName())) {
                            com.cgnpc.drm.entity.Group deviceGroup = groupService.getDeviceGroup(devId, userId);
                            if (deviceGroup != null) {
                                deviceStatus.put("groupName", deviceGroup.getGroupName());
                            } else {
                                deviceStatus.put("groupName", "All");
                            }
                        } else {
                            // 其他分组，直接使用当前分组名称
                            deviceStatus.put("groupName", group.getGroupName());
                        }
                        devices.add(deviceStatus);
                    } catch (Exception e) {
                        // 忽略单个设备的错误，继续处理其他设备
                        continue;
                    }
                }
                groupInfo.put("devices", devices);
            }
            
            // 保存All分组信息，其他分组直接添加
            if ("All".equals(group.getGroupName())) {
                allGroupInfo = groupInfo;
            } else {
                result.add(groupInfo);
            }
        }
        
        // 将All分组插入到列表第一位
        if (allGroupInfo != null) {
            result.add(0, allGroupInfo);
        }
        
        return result;
    }
}