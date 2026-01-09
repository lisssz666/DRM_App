package com.cgnpc.drm.controller;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import com.cgnpc.drm.vo.ResponseVO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/device")
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

      
    /**
     * 获取设备列表
     * GET /api/device/getDeviceList
     */
    @GetMapping("/getDeviceList")
    public ResponseVO<List<Device>> getDeviceList() {
        List<Device> deviceList = deviceService.getAllDevices();
        return ResponseVO.success("获取设备列表成功", deviceList);
    }


    /**
     * 获取设备信息
     * GET /api/device/getDeviceInfo
     */
    @GetMapping("/getDeviceInfo")
    public ResponseVO<Device> getDeviceInfo(@RequestParam String deviceId) {
        Device device = deviceService.getDeviceInfo(deviceId);
        return ResponseVO.success("获取设备信息成功", device);
    }

    /**
     * 控制设备
     * POST /api/device/controlDevice
     */
    @PostMapping("/controlDevice")
    public ResponseVO<Device> controlDevice(
            @RequestParam String deviceId,
            DeviceControlDTO controlDTO) {
        Device device = deviceService.controlDevice(deviceId, controlDTO);
        return ResponseVO.success("设备控制成功", device);
    }

    /**
     * 修改精油名称
     * PUT /api/device/updateOilName
     */
    @PutMapping("/updateOilName")
    public ResponseVO<Device> updateOilName(
            @RequestParam String deviceId,
            @RequestParam String oilName) {
        Device device = deviceService.updateEssentialOilName(deviceId, oilName);
        return ResponseVO.success("精油名称更新成功", device);
    }

    /**
     * 新增设备
     * POST /api/device/addDevice
     */
    @PostMapping("/addDevice")
    public ResponseVO<Device> addDevice(@RequestParam Map<String, String> params) {
        // 打印接收到的参数
        logger.info("接收到新增设备请求，参数: {}", params);
        
        Device device = new Device();
        device.setDeviceName(params.get("deviceName"));
        device.setEssentialOilName(params.get("essentialOilName"));
        
        // 处理整数类型参数 - 使用Optional简化代码
        device.setEssentialOilLevel(Optional.ofNullable(params.get("essentialOilLevel")).map(Integer::parseInt).orElse(null));
        device.setFanSpeed(Optional.ofNullable(params.get("fanSpeed")).map(Integer::parseInt).orElse(null));
        device.setCurrentModeId(Optional.ofNullable(params.get("currentModeId")).map(Long::parseLong).orElse(null));
        
        // 处理布尔类型参数 - 使用Optional简化代码
        device.setFanStatus(Optional.ofNullable(params.get("fanStatus")).map(Boolean::parseBoolean).orElse(null));
        device.setDeviceStatus(Optional.ofNullable(params.get("deviceStatus")).map(Boolean::parseBoolean).orElse(null));
        device.setLockStatus(Optional.ofNullable(params.get("lockStatus")).map(Boolean::parseBoolean).orElse(null));
        device.setLightStatus(Optional.ofNullable(params.get("lightStatus")).map(Boolean::parseBoolean).orElse(null));
        
        logger.info("设备对象构建完成: {}", device);
        
        try {
            Device newDevice = deviceService.addDevice(device);
            logger.info("设备新增成功，结果: {}", newDevice);
            return ResponseVO.success("设备新增成功", newDevice);
        } catch (Exception e) {
            logger.error("设备新增失败: {}", e.getMessage(), e);
            return ResponseVO.error("设备新增失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/deleteDevice")
    public ResponseVO deleteDevice(@RequestParam String deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
            return ResponseVO.success("删除设备成功");
        } catch (Exception e) {
            return ResponseVO.error("删除设备失败: " + e.getMessage());
        }
    }

    // ==============单个控制代码==============
    /**
     * 更新精油量
     * PUT /api/device/updateOilLevel
     */
    @PutMapping("/updateOilLevel")
    public ResponseVO<Device> updateOilLevel(
            @RequestParam String deviceId,
            @RequestParam Integer level) {
        Device device = deviceService.updateEssentialOilLevel(deviceId, level);
        return ResponseVO.success("精油量更新成功", device);
    }

    /**
     * 锁定/解锁设备
     * PUT /api/device/lockDevice
     */
    @PutMapping("/lockDevice")
    public ResponseVO<Device> lockDevice(
            @RequestParam String deviceId,
            @RequestParam Boolean lockStatus) {
        Device device = deviceService.lockDevice(deviceId, lockStatus);
        String message = lockStatus ? "设备锁定成功" : "设备解锁成功";
        return ResponseVO.success(message, device);
    }

    /**
     * 控制风扇开关
     * PUT /api/device/controlFan
     */
    @PutMapping("/controlFan")
    public ResponseVO<Device> controlFan(
            @RequestParam String deviceId,
            @RequestParam Boolean status) {
        Device device = deviceService.controlFan(deviceId, status);
        String message = status ? "风扇开启成功" : "风扇关闭成功";
        return ResponseVO.success(message, device);
    }

    /**
     * 设置风扇速度
     * PUT /api/device/setFanSpeed
     */
    @PutMapping("/setFanSpeed")
    public ResponseVO<Device> setFanSpeed(
            @RequestParam String deviceId,
            @RequestParam Integer speed) {
        Device device = deviceService.setFanSpeed(deviceId, speed);
        return ResponseVO.success("风扇速度设置成功", device);
    }

    /**
     * 控制设备开关
     * PUT /api/device/controlDevicePower
     */
    @PutMapping("/controlDevicePower")
    public ResponseVO<Device> controlDevicePower(
            @RequestParam String deviceId,
            @RequestParam Boolean status) {
        Device device = deviceService.controlDevicePower(deviceId, status);
        String message = status ? "设备开启成功" : "设备关闭成功";
        return ResponseVO.success(message, device);
    }

    /**
     * 控制灯光开关
     * PUT /api/device/controlLight
     */
    @PutMapping("/controlLight")
    public ResponseVO<Device> controlLight(
            @RequestParam String deviceId,
            @RequestParam Boolean status) {
        Device device = deviceService.controlLight(deviceId, status);
        String message = status ? "灯光开启成功" : "灯光关闭成功";
        return ResponseVO.success(message, device);
    }
  
}