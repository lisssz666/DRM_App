package com.cgnpc.drm.repository;

import com.cgnpc.drm.entity.WorkingMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingModeRepository extends JpaRepository<WorkingMode, Long> {
    List<WorkingMode> findByDeviceId(String deviceId);
    List<WorkingMode> findByDeviceIdAndStatus(String deviceId, Boolean status);
}