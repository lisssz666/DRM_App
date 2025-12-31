package com.cgnpc.drm.controller;

import com.cgnpc.drm.entity.WorkingMode;
import com.cgnpc.drm.service.WorkingModeService;
import com.cgnpc.drm.dto.WorkingModeDTO;
import com.cgnpc.drm.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/working-mode")
public class WorkingModeController {

    @Autowired
    private WorkingModeService workingModeService;

    /**
     * 获取设备的所有工作模式
     * GET /api/working-mode/getWorkingModes
     */
    @GetMapping("/getWorkingModes")
    public ResponseVO<List<WorkingMode>> getWorkingModes(@RequestParam String deviceId) {
        List<WorkingMode> modes = workingModeService.getWorkingModes(deviceId);
        return ResponseVO.success(modes);
    }

    /**
     * 获取设备的启用工作模式
     * GET /api/working-mode/getEnabledWorkingModes
     */
    @GetMapping("/getEnabledWorkingModes")
    public ResponseVO<List<WorkingMode>> getEnabledWorkingModes(@RequestParam String deviceId) {
        List<WorkingMode> modes = workingModeService.getEnabledWorkingModes(deviceId);
        return ResponseVO.success(modes);
    }

    /**
     * 添加新的工作模式
     * POST /api/working-mode/addWorkingMode
     */
    @PostMapping("/addWorkingMode")
    public ResponseVO<WorkingMode> addWorkingMode(@RequestBody Map<String, String> params) {
        // 必填字段检查
        if (params.get("deviceId") == null || params.get("deviceId").isEmpty()) {
            return ResponseVO.error("设备ID不能为空");
        }
        
        WorkingModeDTO workingModeDTO = new WorkingModeDTO();
        workingModeDTO.setDeviceId(params.get("deviceId"));
        workingModeDTO.setModeName(params.get("modeName"));
        workingModeDTO.setWeekDays(params.get("weekDays"));
        workingModeDTO.setStartTime(params.get("startTime"));
        workingModeDTO.setEndTime(params.get("endTime"));
        
        // 处理整数类型参数 - 使用Optional简化代码
        workingModeDTO.setRunTime(Optional.ofNullable(params.get("runTime")).map(Integer::parseInt).orElse(null));
        workingModeDTO.setStopTime(Optional.ofNullable(params.get("stopTime")).map(Integer::parseInt).orElse(null));
        
        // 处理布尔类型参数 - 使用Optional简化代码
        workingModeDTO.setIsDefault(Optional.ofNullable(params.get("isDefault")).map(Boolean::parseBoolean).orElse(null));
        workingModeDTO.setStatus(Optional.ofNullable(params.get("status")).map(Boolean::parseBoolean).orElse(null));
        
        WorkingMode mode = workingModeService.addWorkingMode(workingModeDTO);
        return ResponseVO.success("工作模式添加成功", mode);
    }

    /**
     * 更新工作模式
     * PUT /api/working-mode/updateWorkingMode
     */
    @PutMapping("/updateWorkingMode")
    public ResponseVO<WorkingMode> updateWorkingMode(
            @RequestParam Long id,
            WorkingModeDTO workingModeDTO) {
        WorkingMode mode = workingModeService.updateWorkingMode(id, workingModeDTO);
        return ResponseVO.success("工作模式更新成功", mode);
    }

    /**
     * 删除工作模式
     * DELETE /api/working-mode/deleteWorkingMode
     */
    @DeleteMapping("/deleteWorkingMode")
    public ResponseVO<Void> deleteWorkingMode(@RequestParam Long id) {
        workingModeService.deleteWorkingMode(id);
        return ResponseVO.success("工作模式删除成功");
    }

    /**
     * 获取工作模式详情
     * GET /api/working-mode/getWorkingModeById
     */
    @GetMapping("/getWorkingModeById")
    public ResponseVO<WorkingMode> getWorkingModeById(@RequestParam Long id) {
        WorkingMode mode = workingModeService.getWorkingModeById(id);
        return ResponseVO.success(mode);
    }
}