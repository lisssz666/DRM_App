package com.cgnpc.drm.repository;

import com.cgnpc.drm.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 反馈问题仓库
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 根据用户ID查询反馈列表
     * @param userId 用户ID
     * @return 反馈列表
     */
    List<Feedback> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据状态查询反馈列表
     * @param status 状态
     * @return 反馈列表
     */
    List<Feedback> findByStatusOrderByCreateTimeDesc(Integer status);
}
