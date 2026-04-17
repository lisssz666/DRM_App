package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "device_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false)
    private String groupName; // 分组名称

    @Column(name = "is_default")
    private Boolean isDefault = false; // 是否为默认分组（All分组）

    @Column(name = "user_id", nullable = false)
    private Long userId; // 所属用户ID

    @Column(name = "description")
    private String description; // 分组描述

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
        updatedTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = new Date();
    }
}