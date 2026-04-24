package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.dto.DeviceControlDTO;
import java.util.List;
import java.util.Map;

public interface DeviceService {
    /**
     * 获取设备信息
     * @param deviceId 设备ID
     * @return 设备信息
     */
    Device getDeviceInfo(String deviceId);

    /**
     * 控制设备
     * @param deviceId 设备ID
     * @param controlDTO 控制参数
     * @return 更新后的设备信息
     */
    Device controlDevice(String deviceId, DeviceControlDTO controlDTO);

    /**
     * 更新设备基本信息
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param essentialOilName 精油名称
     * @return 更新后的设备信息
     */
    Device updateDeviceInfo(String deviceId, String deviceName, String essentialOilName);

    /**
     * 新增设备
     * @param device 设备信息
     * @return 新增的设备信息
     */
    Device addDevice(Device device);
    
    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 删除结果
     */
    boolean deleteDevice(String deviceId);
    
    /**
     * 获取所有设备列表
     * @return 设备列表
     */
    List<Device> getAllDevices();

    /**
     * 根据用户ID获取设备列表
     * @param userId 用户ID
     * @return 设备列表
     */
    List<Device> getDevicesByUserId(Long userId);

    /**
     * 验证设备是否属于指定用户
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 验证结果
     */
    boolean validateDeviceOwnership(String deviceId, Long userId);

    /**
     * 设备恢复出厂设置
     * @param deviceId 设备ID
     * @return 恢复后的设备信息
     */
    Device resetDeviceToFactorySettings(String deviceId);

    /**
     * 重置气泵使用时间
     * @param deviceId 设备ID
     * @return 更新后的设备信息
     */
    Device resetPumpUsageTime(String deviceId);

    /**
     * 获取设备状态信息
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    Map<String, Object> getDeviceStatusInfo(String deviceId);

    /**
     * 获取分组下的设备信息（含模式）
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param includeDevices 是否包含设备详情（默认true）
     * @return 分组设备信息列表
     */
    List<Map<String, Object>> getGroupDevicesInfo(String deviceId, Long userId, boolean includeDevices);
}