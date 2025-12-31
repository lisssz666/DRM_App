package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.repository.DeviceRepository;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

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
        }
        if (controlDTO.getDeviceStatus() != null) {
            device.setDeviceStatus(controlDTO.getDeviceStatus());
        }
        if (controlDTO.getLockStatus() != null) {
            device.setLockStatus(controlDTO.getLockStatus());
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
    public Device addDevice(Device device) {
        // 检查设备ID是否已存在
        if (deviceRepository.findByDeviceId(device.getDeviceId()) != null) {
            throw new RuntimeException("设备ID已存在");
        }
        
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
        if (device.getFanSpeed() == null) {
            device.setFanSpeed(0);
        }
        if (device.getEssentialOilLevel() == null) {
            device.setEssentialOilLevel(100);
        }
        
        return deviceRepository.save(device);
    }
}