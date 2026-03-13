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
     * 修改精油名称
     * @param deviceId 设备ID
     * @param oilName 精油名称
     * @return 更新后的设备信息
     */
    Device updateEssentialOilName(String deviceId, String oilName);

    /**
     * 更新精油量
     * @param deviceId 设备ID
     * @param level 精油量（0-100）
     * @return 更新后的设备信息
     */
    Device updateEssentialOilLevel(String deviceId, Integer level);

    /**
     * 锁定/解锁设备
     * @param deviceId 设备ID
     * @param lockStatus 锁定状态
     * @return 更新后的设备信息
     */
    Device lockDevice(String deviceId, Boolean lockStatus);

    /**
     * 控制风扇开关
     * @param deviceId 设备ID
     * @param status 风扇状态
     * @return 更新后的设备信息
     */
    Device controlFan(String deviceId, Boolean status);

    /**
     * 设置风扇速度
     * @param deviceId 设备ID
     * @param speed 风扇速度（0-100）
     * @return 更新后的设备信息
     */
    Device setFanSpeed(String deviceId, Integer speed);

    /**
     * 控制设备开关
     * @param deviceId 设备ID
     * @param status 设备状态
     * @return 更新后的设备信息
     */
    Device controlDevicePower(String deviceId, Boolean status);

    /**
     * 控制灯光开关
     * @param deviceId 设备ID
     * @param status 灯光状态
     * @return 更新后的设备信息
     */
    Device controlLight(String deviceId, Boolean status);

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
}