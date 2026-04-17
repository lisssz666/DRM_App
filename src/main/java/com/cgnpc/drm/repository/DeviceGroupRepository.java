package com.cgnpc.drm.repository;

import com.cgnpc.drm.entity.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, Long> {
    List<DeviceGroup> findByUserId(Long userId);
    List<DeviceGroup> findByGroupIdAndUserId(Long groupId, Long userId);
    DeviceGroup findByDeviceIdAndUserId(String deviceId, Long userId);
    void deleteByDeviceIdAndUserId(String deviceId, Long userId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}