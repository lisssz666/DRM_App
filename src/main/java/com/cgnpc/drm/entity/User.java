package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 邮箱（用于登录）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 手机号（用于登录）
     */
    @Column(unique = true, length = 20)
    private String phone;

    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 用户名
     */
    @Column(length = 50)
    private String username;

    /**
     * 用户状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}