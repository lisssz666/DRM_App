package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.dto.DeviceControlDTO;

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
}