package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.entity.WorkingMode;
import com.cgnpc.drm.repository.DeviceRepository;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.service.MQTTService;
import com.cgnpc.drm.service.MQTTMessageHandlerService;
import com.cgnpc.drm.service.WorkingModeService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MQTTService mqttService;

    @Autowired
    private MQTTMessageHandlerService mqttMessageHandlerService;

    @Autowired
    private WorkingModeService workingModeService;

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
    public Device updateEssentialOilName(String deviceId, String oilName) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setEssentialOilName(oilName);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device updateEssentialOilLevel(String deviceId, Integer level) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setEssentialOilLevel(level);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device lockDevice(String deviceId, Boolean lockStatus) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setLockStatus(lockStatus);
        // 发送锁定控制命令
        mqttService.sendLockCommand(deviceId, device.getDeviceId(), lockStatus);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlFan(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setFanStatus(status);
        // 发送风扇控制命令
        mqttService.sendFanCommand(deviceId, device.getDeviceId(), status ? 1 : 0);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device setFanSpeed(String deviceId, Integer speed) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setFanSpeed(speed);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlDevicePower(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setDeviceStatus(status);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlLight(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        device.setLightStatus(status);
        // 发送灯光控制命令
        mqttService.sendLightCommand(deviceId, device.getDeviceId(), status);
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
    public Device resetPumpUsageTime(String deviceId) {
        // 调用MQTTMessageHandlerService重置气泵使用时间
        mqttMessageHandlerService.resetPumpUsageTime(deviceId);
        // 返回更新后的设备信息
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public Map<String, Object> getDeviceStatusInfo(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device does not exist.");  // 设备不存在
        }

        // 构建设备状态信息
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("deviceId", device.getDeviceId());
        statusInfo.put("deviceName", device.getDeviceName());
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

        // 添加状态描述
        statusInfo.put("devicePostureDesc", device.getDevicePosture() == null ? "Unknown" : (device.getDevicePosture() == 0 ? "Upright" : "Tilted"));  // 竖立 : 倾倒
        statusInfo.put("liquidLevelDesc", getLiquidLevelDescription(device.getLiquidLevel()));

        // 添加启用的工作模式信息
        List<WorkingMode> enabledModes = workingModeService.getEnabledWorkingModes(deviceId);
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
}