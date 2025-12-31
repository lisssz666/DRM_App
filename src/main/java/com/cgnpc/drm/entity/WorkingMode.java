package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "working_mode")
public class WorkingMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // 关联的设备ID

    @Column(name = "mode_name")
    private String modeName; // 模式名称

    @Column(name = "week_days")
    private String weekDays; // 工作周几，用逗号分隔，如"Mon,Tue,Wed,Thu,Fri"

    @Column(name = "start_time")
    private String startTime; // 工作开始时间，格式HH:mm

    @Column(name = "end_time")
    private String endTime; // 工作结束时间，格式HH:mm

    @Column(name = "run_time")
    private Integer runTime; // 运行时间（秒）

    @Column(name = "stop_time")
    private Integer stopTime; // 停止时间（秒）

    @Column(name = "is_default")
    private Boolean isDefault; // 是否为默认模式

    @Column(name = "status")
    private Boolean status; // 模式启用状态

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
}