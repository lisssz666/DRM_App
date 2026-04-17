package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "device_group_relation")
public class DeviceGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // 设备ID

    @Column(name = "group_id", nullable = false)
    private Long groupId; // 分组ID

    @Column(name = "user_id", nullable = false)
    private Long userId; // 所属用户ID

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
    }
}