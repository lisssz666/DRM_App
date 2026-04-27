package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.Feedback;
import com.cgnpc.drm.dto.FeedbackDTO;
import java.util.List;

/**
 * 反馈问题服务
 */
public interface FeedbackService {

    /**
     * 提交反馈
     * @param userId 用户ID
     * @param feedbackDTO 反馈信息
     * @return 反馈信息
     */
    Feedback submitFeedback(Long userId, FeedbackDTO feedbackDTO);

    /**
     * 获取用户的反馈列表
     * @param userId 用户ID
     * @return 反馈列表
     */
    List<Feedback> getUserFeedbacks(Long userId);

    /**
     * 获取所有反馈列表（管理员）
     * @param status 状态
     * @return 反馈列表
     */
    List<Feedback> getAllFeedbacks(Integer status);

    /**
     * 处理反馈
     * @param id 反馈ID
     * @return 处理结果
     */
    boolean processFeedback(Long id);
}
