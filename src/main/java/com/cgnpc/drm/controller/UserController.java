package com.cgnpc.drm.controller;

import com.cgnpc.drm.dto.ForgotPasswordDTO;
import com.cgnpc.drm.dto.UserLoginDTO;
import com.cgnpc.drm.dto.UserRegisterDTO;
import com.cgnpc.drm.entity.User;
import com.cgnpc.drm.service.UserService;
import com.cgnpc.drm.util.JwtUtil;
import com.cgnpc.drm.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录（支持密码登录和验证码登录）
     */
    @PostMapping("/login")
    public ResponseVO<Map<String, Object>> login(@RequestParam Map<String, String> params) {
        try {
            UserLoginDTO loginDTO = new UserLoginDTO();
            loginDTO.setEmail(params.get("email"));
            loginDTO.setPhone(params.get("phone"));
            loginDTO.setPassword(params.get("password"));
            loginDTO.setCode(params.get("code"));
            loginDTO.setLoginType(params.get("loginType"));
            
            User user = userService.login(loginDTO);
            
            // 生成 token，使用邮箱作为用户名（如果没有邮箱则使用手机号）
            String username = user.getEmail() != null ? user.getEmail() : user.getPhone();
            String token = jwtUtil.generateToken(username);
            
            // 清除密码，确保不返回给前端
            user.setPassword(null);
            
            // 返回用户信息和 token
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", token);
            
            return ResponseVO.success("登录成功", data);
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseVO<User> register(@RequestParam Map<String, String> params) {
        try {
            UserRegisterDTO registerDTO = new UserRegisterDTO();
            registerDTO.setEmail(params.get("email"));
            registerDTO.setPhone(params.get("phone"));
            registerDTO.setPassword(params.get("password"));
            registerDTO.setConfirmPassword(params.get("confirmPassword"));
            registerDTO.setCode(params.get("code"));
            
            User user = userService.register(registerDTO);
            // 清除密码，确保不返回给前端
            user.setPassword(null);
            return ResponseVO.success("注册成功", user);
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 发送验证码（支持邮箱和手机号）
     */
    @PostMapping("/sendCode")
    public ResponseVO<Void> sendCode(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam String type) {
        try {
            // 验证邮箱或手机号是否提供
            if ((email == null || email.isEmpty()) && (phone == null || phone.isEmpty())) {
                return ResponseVO.error(400, "Email or phone number cannot be empty.");
            }
            
            // 根据type进行验证
            if ("login".equals(type)) {
                // 登录类型：检查账号是否存在
                if (email != null && !email.isEmpty()) {
                    if (!userService.existsByEmail(email)) {
                        return ResponseVO.error(400, "This's not registered account.");
                    }
                } else if (phone != null && !phone.isEmpty()) {
                    if (!userService.existsByPhone(phone)) {
                        return ResponseVO.error(400, "This's not registered account.");
                    }
                }
            } else if ("register".equals(type)) {
                // 注册类型：检查账号是否已存在
                if (email != null && !email.isEmpty()) {
                    if (userService.existsByEmail(email)) {
                        return ResponseVO.error(400, "This email has already been registered.");
                    }
                } else if (phone != null && !phone.isEmpty()) {
                    if (userService.existsByPhone(phone)) {
                        return ResponseVO.error(400, "This phone number has already been registered.");
                    }
                }
            }
            
            userService.sendCode(email, phone, type);
            return ResponseVO.success("验证码发送成功");
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verifyCode")
    public ResponseVO<Void> verifyCode(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam String code,
            @RequestParam String type) {
        try {
            boolean isValid = userService.verifyCode(email, phone, code, type);
            if (isValid) {
                return ResponseVO.success("验证码验证成功");
            } else {
                return ResponseVO.error(400, "验证码错误或已过期");
            }
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 忘记密码，重置密码
     */
    @PostMapping("/forgotPassword")
    public ResponseVO<Void> forgotPassword(@RequestParam Map<String, String> params) {
        try {
            ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
            forgotPasswordDTO.setEmail(params.get("email"));
            forgotPasswordDTO.setCode(params.get("code"));
            forgotPasswordDTO.setNewPassword(params.get("newPassword"));
            forgotPasswordDTO.setConfirmPassword(params.get("confirmPassword"));
            
            boolean success = userService.forgotPassword(forgotPasswordDTO);
            if (success) {
                return ResponseVO.success("密码重置成功");
            } else {
                return ResponseVO.error(400, "密码重置失败");
            }
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 检查邮箱是否已存在
     */
    @GetMapping("/checkEmail")
    public ResponseVO<Boolean> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResponseVO.success("邮箱检查成功", exists);
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 检查手机号是否已存在
     */
    @GetMapping("/checkPhone")
    public ResponseVO<Boolean> checkPhoneExists(@RequestParam String phone) {
        try {
            boolean exists = userService.existsByPhone(phone);
            return ResponseVO.success("手机号检查成功", exists);
        } catch (Exception e) {
            return ResponseVO.error(500, e.getMessage());
        }
    }

    /**
     * 用户退出登录
     * 支持两种方式传入token：
     * 1. 请求头Authorization: Bearer {token}
     * 2. 请求参数token={token}
     * 第二种方式仅用于测试
     */
    @PostMapping("/logout")
    public ResponseVO<Void> logout(HttpServletRequest request, @RequestParam(required = false) String token) {
        try {
            // 优先从请求头获取token
            if (token == null || token.isEmpty()) {
                final String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    token = authorizationHeader.substring(7);
                }
            }
            
            if (token != null && !token.isEmpty()) {
                // 实现token失效逻辑
                userService.logout(token);
                logger.info("用户退出登录成功，token已加入黑名单");
            } else {
                logger.warn("退出登录时未提供token");
            }
            return ResponseVO.success("退出登录成功");
        } catch (Exception e) {
            logger.error("退出登录失败: {}", e.getMessage());
            return ResponseVO.error(500, "退出登录失败: " + e.getMessage());
        }
    }
}