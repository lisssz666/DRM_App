package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Feedback;
import com.cgnpc.drm.dto.FeedbackDTO;
import com.cgnpc.drm.repository.FeedbackRepository;
import com.cgnpc.drm.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈问题服务实现
 */
@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback submitFeedback(Long userId, FeedbackDTO feedbackDTO) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(feedbackDTO.getContent());
        feedback.setContact(feedbackDTO.getContact());
        feedback.setStatus(0); // 0-未处理

        Feedback savedFeedback = feedbackRepository.save(feedback);
        logger.info("User submitted feedback: userId={}, feedbackId={}", userId, savedFeedback.getId());
        return savedFeedback;
    }

    @Override
    public List<Feedback> getUserFeedbacks(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<Feedback> getAllFeedbacks(Integer status) {
        if (status != null) {
            return feedbackRepository.findByStatusOrderByCreateTimeDesc(status);
        } else {
            return feedbackRepository.findAll();
        }
    }

    @Override
    public boolean processFeedback(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        feedback.setStatus(1); // 1-已处理
        feedback.setProcessTime(LocalDateTime.now());
        feedbackRepository.save(feedback);
        
        logger.info("Feedback processed: id={}", id);
        return true;
    }
}
