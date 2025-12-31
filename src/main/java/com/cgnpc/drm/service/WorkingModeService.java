package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.WorkingMode;
import com.cgnpc.drm.dto.WorkingModeDTO;

import java.util.List;

public interface WorkingModeService {
    /**
     * 获取设备的所有工作模式
     * @param deviceId 设备ID
     * @return 工作模式列表
     */
    List<WorkingMode> getWorkingModes(String deviceId);

    /**
     * 获取设备的启用工作模式
     * @param deviceId 设备ID
     * @return 启用的工作模式列表
     */
    List<WorkingMode> getEnabledWorkingModes(String deviceId);

    /**
     * 添加新的工作模式
     * @param workingModeDTO 工作模式数据
     * @return 添加的工作模式
     */
    WorkingMode addWorkingMode(WorkingModeDTO workingModeDTO);

    /**
     * 更新工作模式
     * @param id 工作模式ID
     * @param workingModeDTO 工作模式数据
     * @return 更新后的工作模式
     */
    WorkingMode updateWorkingMode(Long id, WorkingModeDTO workingModeDTO);

    /**
     * 删除工作模式
     * @param id 工作模式ID
     */
    void deleteWorkingMode(Long id);

    /**
     * 获取工作模式详情
     * @param id 工作模式ID
     * @return 工作模式详情
     */
    WorkingMode getWorkingModeById(Long id);
}