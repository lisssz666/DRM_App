package com.cgnpc.drm.repository;

import com.cgnpc.drm.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Device findByDeviceId(String deviceId);
}