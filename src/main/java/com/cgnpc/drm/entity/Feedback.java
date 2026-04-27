package com.cgnpc.drm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 反馈问题实体类
 */
@Data
@Entity
@Table(name = "sys_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 问题和反馈内容
     */
    @Column(nullable = false, length = 200)
    private String content;

    /**
     * 联系方式
     */
    @Column(length = 100)
    private String contact;

    /**
     * 状态：0-未处理，1-已处理
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
