package com.cgnpc.drm.controller;

import com.cgnpc.drm.dto.FeedbackDTO;
import com.cgnpc.drm.service.FeedbackService;
import com.cgnpc.drm.vo.ResponseVO;
import com.cgnpc.drm.util.JwtUtil;
import com.cgnpc.drm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 反馈问题控制器
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 提交反馈
     * @param params 反馈信息
     * @param request 请求对象
     * @return 响应结果
     */
    @PostMapping("/submit")
    public ResponseVO<?> submitFeedback(@RequestParam java.util.Map<String, String> params, HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getCurrentUserId(request);
            
            // 构建反馈DTO
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            feedbackDTO.setContent(params.get("content"));
            feedbackDTO.setContact(params.get("contact"));
            
            // 提交反馈
            feedbackService.submitFeedback(userId, feedbackDTO);
            
            return ResponseVO.success("反馈提交成功");
        } catch (Exception e) {
            logger.error("提交反馈失败: {}", e.getMessage());
            return ResponseVO.error("反馈提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取反馈列表
     * @param admin 是否获取所有用户的反馈（true-获取所有，false-获取当前用户的）
     * @param status 状态（0-未处理，1-已处理，null-全部）
     * @param request 请求对象
     * @return 响应结果
     */
    @GetMapping("/list")
    public ResponseVO<?> getFeedbacks(@RequestParam(required = false, defaultValue = "false") boolean admin, 
                                    @RequestParam(required = false) Integer status, 
                                    HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getCurrentUserId(request);
            
            if (admin) {
                // 获取所有反馈
                return ResponseVO.success("获取反馈列表成功", feedbackService.getAllFeedbacks(status));
            } else {
                // 获取当前用户的反馈列表
                return ResponseVO.success("获取反馈列表成功", feedbackService.getUserFeedbacks(userId));
            }
        } catch (Exception e) {
            logger.error("获取反馈列表失败: {}", e.getMessage());
            return ResponseVO.error("获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理反馈（管理员）
     * @param id 反馈ID
     * @param request 请求对象
     * @return 响应结果
     */
    @PostMapping("/process")
    public ResponseVO<?> processFeedback(@RequestParam Long id, HttpServletRequest request) {
        try {
            // 从token中获取用户ID（验证登录）
            getCurrentUserId(request);
            
            // 处理反馈
            feedbackService.processFeedback(id);
            
            return ResponseVO.success("处理反馈成功");
        } catch (Exception e) {
            logger.error("处理反馈失败: {}", e.getMessage());
            return ResponseVO.error("处理反馈失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 从token中提取用户名（邮箱或手机号）
            String username = jwtUtil.extractUsername(token);
            // 根据用户名获取用户对象（先尝试邮箱，再尝试手机号）
            com.cgnpc.drm.entity.User user = null;
            if (username.contains("@")) {
                // 邮箱登录
                user = userService.findByEmail(username);
            } else {
                // 手机号登录
                user = userService.findByPhone(username);
            }
            if (user != null) {
                return user.getId();
            }
        }
        throw new RuntimeException("User not found or invalid token");
    }
}
