package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.repository.DeviceRepository;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.service.MQTTService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MQTTService mqttService;

    @Override
    public Device getDeviceInfo(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public Device controlDevice(String deviceId, DeviceControlDTO controlDTO) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        if (controlDTO.getFanStatus() != null) {
            device.setFanStatus(controlDTO.getFanStatus());
            // 发送风扇控制命令
            mqttService.sendFanCommand(deviceId, controlDTO.getFanStatus() ? 1 : 0);
        }
        if (controlDTO.getDeviceStatus() != null) {
            device.setDeviceStatus(controlDTO.getDeviceStatus());
        }
        if (controlDTO.getLockStatus() != null) {
            device.setLockStatus(controlDTO.getLockStatus());
            // 发送锁定控制命令
            mqttService.sendLockCommand(deviceId, controlDTO.getLockStatus() ? 1 : 0);
        }
        if (controlDTO.getLightStatus() != null) {
            device.setLightStatus(controlDTO.getLightStatus());
            // 发送灯光控制命令
            mqttService.sendLightCommand(deviceId, controlDTO.getLightStatus() ? 1 : 0);
        }
        if (controlDTO.getFanSpeed() != null) {
            device.setFanSpeed(controlDTO.getFanSpeed());
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

        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device updateEssentialOilName(String deviceId, String oilName) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setEssentialOilName(oilName);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device updateEssentialOilLevel(String deviceId, Integer level) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setEssentialOilLevel(level);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device lockDevice(String deviceId, Boolean lockStatus) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setLockStatus(lockStatus);
        // 发送锁定控制命令
        mqttService.sendLockCommand(deviceId, lockStatus ? 1 : 0);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlFan(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setFanStatus(status);
        // 发送风扇控制命令
        mqttService.sendFanCommand(deviceId, status ? 1 : 0);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device setFanSpeed(String deviceId, Integer speed) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setFanSpeed(speed);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlDevicePower(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setDeviceStatus(status);
        device.setUpdatedTime(new Date());
        return deviceRepository.save(device);
    }

    @Override
    public Device controlLight(String deviceId, Boolean status) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        device.setLightStatus(status);
        // 发送灯光控制命令
        mqttService.sendLightCommand(deviceId, status ? 1 : 0);
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
        throw new RuntimeException("无法生成唯一设备ID，请稍后重试");
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
            throw new RuntimeException("设备不存在");
        }
        
        deviceRepository.delete(device);
        return true;
    }
    
    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}