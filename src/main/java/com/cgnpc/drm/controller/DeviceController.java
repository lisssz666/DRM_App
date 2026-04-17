package com.cgnpc.drm.controller;

import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.service.DeviceService;
import com.cgnpc.drm.dto.DeviceControlDTO;
import com.cgnpc.drm.vo.ResponseVO;
import com.cgnpc.drm.util.JwtUtil;
import com.cgnpc.drm.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 生成设备型号：大写字母+3个随机数字
     */
    private String generateDeviceModel() {
        // 生成3个大写字母
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            char c = (char) (65 + (int) (Math.random() * 26)); // 65是'A'的ASCII码
            sb.append(c);
        }
        // 生成3个随机数字
        for (int i = 0; i < 3; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 从token中提取用户名（邮箱或手机号）
            String username = jwtUtil.extractUsername(token);
            // 根据用户名获取用户对象（先尝试邮箱，再尝试手机号）
            com.cgnpc.drm.entity.User user = null;
            if (username.contains("@")) {
                // 邮箱登录
                user = userService.findByEmail(username);
            } else {
                // 手机号登录
                user = userService.findByPhone(username);
            }
            if (user != null) {
                return user.getId();
            }
        }
        throw new RuntimeException("User not logged in");
    }

      
    /**
     * 获取设备列表
     * GET /api/device/getDeviceList
     */
    @GetMapping("/getDeviceList")
    public ResponseVO<List<Device>> getDeviceList(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Device> deviceList = deviceService.getDevicesByUserId(userId);
        return ResponseVO.success("获取设备列表成功", deviceList);
    }


    /**
     * 获取设备信息
     * GET /api/device/getDeviceInfo
     */
    @GetMapping("/getDeviceInfo")
    public ResponseVO<Device> getDeviceInfo(@RequestParam String deviceId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
            return ResponseVO.error(403, "You don't have permission to access this device");
        }
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
            DeviceControlDTO controlDTO,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
            return ResponseVO.error(403, "You don't have permission to control this device");
        }
        Device device = deviceService.controlDevice(deviceId, controlDTO);
        return ResponseVO.success("设备控制成功", device);
    }

    /**
     * 更新设备基本信息（设备名称、精油名称）
     * PUT /api/device/updateDeviceInfo
     */
    @PutMapping("/updateDeviceInfo")
    public ResponseVO<Device> updateDeviceInfo(
            @RequestParam String deviceId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String essentialOilName,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
            return ResponseVO.error(403, "You don't have permission to update this device");
        }
        try {
            Device device = deviceService.updateDeviceInfo(deviceId, deviceName, essentialOilName);
            return ResponseVO.success("设备信息更新成功", device);
        } catch (Exception e) {
            return ResponseVO.error(500, "设备信息更新失败: " + e.getMessage());
        }
    }

    /**
     * 新增设备
     * POST /api/device/addDevice
     */
    @PostMapping("/addDevice")
    public ResponseVO<Device> addDevice(@RequestParam Map<String, String> params, HttpServletRequest request) {
        // 打印接收到的参数
        logger.info("接收到新增设备请求，参数: {}", params);
        
        Device device = new Device();
        device.setDeviceName(params.get("deviceName"));
        device.setEssentialOilName(params.get("essentialOilName"));
        
        // 生成设备型号：大写字母+3个随机数字
        String model = generateDeviceModel();
        device.setModel(model);
        
        // 设置用户ID
        Long userId = getCurrentUserId(request);
        device.setUserId(userId);
        
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
    public ResponseVO deleteDevice(@RequestParam String deviceId, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
                return ResponseVO.error(403, "You don't have permission to delete this device");
            }
            deviceService.deleteDevice(deviceId);
            return ResponseVO.success("删除设备成功");
        } catch (Exception e) {
            return ResponseVO.error("删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 设备恢复出厂设置
     * POST /api/device/resetDevice
     */
    @PostMapping("/resetDevice")
    public ResponseVO<Device> resetDevice(@RequestParam String deviceId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
            return ResponseVO.error(403, "You don't have permission to reset this device");
        }
        try {
            Device device = deviceService.resetDeviceToFactorySettings(deviceId);
            return ResponseVO.success("设备恢复出厂设置成功", device);
        } catch (Exception e) {
            return ResponseVO.error(500, "设备恢复出厂设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备状态信息
     * GET /api/device/getDeviceStatusInfo
     */
    @GetMapping("/getDeviceStatusInfo")
    public ResponseVO<Map<String, Object>> getDeviceStatusInfo(@RequestParam String deviceId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (!deviceService.validateDeviceOwnership(deviceId, userId)) {
            return ResponseVO.error(403, "You don't have permission to access this device");
        }
        Map<String, Object> statusInfo = deviceService.getDeviceStatusInfo(deviceId);
        return ResponseVO.success("获取设备状态信息成功", statusInfo);
    }

    /**
     * 获取分组下的设备信息（含模式）
     * GET /api/device/getGroupDevices
     */
    @GetMapping("/getGroupDevices")
    public ResponseVO<List<Map<String, Object>>> getGroupDevices(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false, defaultValue = "true") boolean includeDevices,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            List<Map<String, Object>> groupDevicesInfo = deviceService.getGroupDevicesInfo(groupId, deviceId, userId, includeDevices);
            return ResponseVO.success("获取分组设备信息成功", groupDevicesInfo);
        } catch (Exception e) {
            return ResponseVO.error(500, "获取分组设备信息失败: " + e.getMessage());
        }
    }
  
}