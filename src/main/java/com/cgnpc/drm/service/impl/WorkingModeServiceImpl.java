package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.WorkingMode;
import com.cgnpc.drm.repository.WorkingModeRepository;
import com.cgnpc.drm.service.WorkingModeService;
import com.cgnpc.drm.dto.WorkingModeDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WorkingModeServiceImpl implements WorkingModeService {

    @Autowired
    private WorkingModeRepository workingModeRepository;

    @Override
    public List<WorkingMode> getWorkingModes(String deviceId) {
        return workingModeRepository.findByDeviceId(deviceId);
    }

    @Override
    public List<WorkingMode> getEnabledWorkingModes(String deviceId) {
        return workingModeRepository.findByDeviceIdAndStatus(deviceId, true);
    }

    @Override
    public WorkingMode addWorkingMode(WorkingModeDTO workingModeDTO) {
        WorkingMode workingMode = new WorkingMode();
        BeanUtils.copyProperties(workingModeDTO, workingMode);
        
        // 设置默认值
        if (workingMode.getIsDefault() == null) {
            workingMode.setIsDefault(false);
        }
        if (workingMode.getStatus() == null) {
            workingMode.setStatus(true);
        }
        if (workingMode.getRunTime() == null) {
            workingMode.setRunTime(0);
        }
        if (workingMode.getStopTime() == null) {
            workingMode.setStopTime(0);
        }
        
        workingMode.setCreatedTime(new Date());
        workingMode.setUpdatedTime(new Date());
        return workingModeRepository.save(workingMode);
    }

    @Override
    public WorkingMode updateWorkingMode(Long id, WorkingModeDTO workingModeDTO) {
        WorkingMode workingMode = workingModeRepository.findById(id).orElseThrow(() -> new RuntimeException("工作模式不存在"));
        BeanUtils.copyProperties(workingModeDTO, workingMode);
        workingMode.setId(id);
        workingMode.setUpdatedTime(new Date());
        return workingModeRepository.save(workingMode);
    }

    @Override
    public void deleteWorkingMode(Long id) {
        workingModeRepository.deleteById(id);
    }

    @Override
    public WorkingMode getWorkingModeById(Long id) {
        return workingModeRepository.findById(id).orElse(null);
    }
}