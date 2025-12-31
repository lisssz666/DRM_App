package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId; // 设备ID（如P205）

    @Column(name = "device_name")
    private String deviceName; // 设备名称

    @Column(name = "essential_oil_name")
    private String essentialOilName; // 精油名称

    @Column(name = "essential_oil_level")
    private Integer essentialOilLevel; // 精油量（0-100%）

    @Column(name = "fan_status")
    private Boolean fanStatus; // 风扇开关（true: 开，false: 关）

    @Column(name = "device_status")
    private Boolean deviceStatus; // 设备开关（true: 开，false: 关）

    @Column(name = "lock_status")
    private Boolean lockStatus; // 锁定状态（true: 锁定，false: 解锁）

    @Column(name = "light_status")
    private Boolean lightStatus; // 灯光状态（true: 开启，false: 关闭）

    @Column(name = "fan_speed")
    private Integer fanSpeed; // 风扇速度（0-100）

    @Column(name = "current_mode_id")
    private Long currentModeId; // 当前工作模式ID

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
}