package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.dto.ForgotPasswordDTO;
import com.cgnpc.drm.dto.UserLoginDTO;
import com.cgnpc.drm.dto.UserRegisterDTO;
import com.cgnpc.drm.entity.User;
import com.cgnpc.drm.repository.UserRepository;
import com.cgnpc.drm.service.MailService;
import com.cgnpc.drm.service.SmsService;
import com.cgnpc.drm.service.UserService;
import com.cgnpc.drm.service.VerificationCodeService;
import com.cgnpc.drm.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private MailService mailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User login(UserLoginDTO loginDTO) {
        Assert.notNull(loginDTO, "Login information cannot be empty.");  // 登录信息不能为空

        User user;

        if ("password".equals(loginDTO.getLoginType())) {
            // 密码登录
            if (loginDTO.getEmail() != null && !loginDTO.getEmail().isEmpty()) {
                user = userRepository.findByEmail(loginDTO.getEmail())
                        .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
            } else if (loginDTO.getPhone() != null && !loginDTO.getPhone().isEmpty()) {
                user = userRepository.findByPhone(loginDTO.getPhone())
                        .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
            } else {
                throw new RuntimeException("Login account cannot be empty.");  // 登录账号不能为空
            }

            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new RuntimeException("Incorrect password.");  // 密码错误
            }
        } else if ("code".equals(loginDTO.getLoginType())) {
            // 验证码登录
            if (loginDTO.getEmail() != null && !loginDTO.getEmail().isEmpty()) {
                user = userRepository.findByEmail(loginDTO.getEmail())
                        .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
                // 验证邮箱验证码
                if (!verificationCodeService.verifyCode(loginDTO.getEmail(), loginDTO.getCode(), "login")) {
                    throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
                }
            } else if (loginDTO.getPhone() != null && !loginDTO.getPhone().isEmpty()) {
                user = userRepository.findByPhone(loginDTO.getPhone())
                        .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
                // 验证短信验证码
                if (!verificationCodeService.verifyCode(loginDTO.getPhone(), loginDTO.getCode(), "login")) {
                    throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
                }
            } else {
                throw new RuntimeException("Login account cannot be empty.");  // 登录账号不能为空
            }
        } else {
            throw new RuntimeException("Unsupported login type.");  // 不支持的登录类型
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("User account has been disabled.");  // 用户已被禁用
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    @Override
    public User register(UserRegisterDTO registerDTO) {
        Assert.notNull(registerDTO, "Registration information cannot be empty.");  // 注册信息不能为空

        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("The two passwords do not match.");  // 两次输入的密码不一致
        }

        boolean isEmailRegister = registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty();
        boolean isPhoneRegister = registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty();

        // 验证验证码
        if (isEmailRegister) {
            // 邮箱注册
            // 检查邮箱是否已注册
            if (userRepository.existsByEmail(registerDTO.getEmail())) {
                throw new RuntimeException("This email has already been registered.");  // 邮箱已被注册
            }
            // 验证邮箱验证码
            if (!verificationCodeService.verifyCode(registerDTO.getEmail(), registerDTO.getCode(), "register")) {
                throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
            }
        } else if (isPhoneRegister) {
            // 手机号注册
            // 检查手机号是否已注册
            if (userRepository.existsByPhone(registerDTO.getPhone())) {
                throw new RuntimeException("This phone number has already been registered.");  // 手机号已被注册
            }
            // 验证手机号验证码
            if (!verificationCodeService.verifyCode(registerDTO.getPhone(), registerDTO.getCode(), "register")) {
                throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
            }
        } else {
            throw new RuntimeException("Please provide at least one email or phone number.");  // 邮箱或手机号至少提供一个
        }

        // 创建用户
        User user = new User();
        user.setEmail("".equals(registerDTO.getEmail()) ? null : registerDTO.getEmail());
        user.setPhone("".equals(registerDTO.getPhone()) ? null : registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        // 设置用户名
        if (isEmailRegister) {
            // 邮箱注册：用户名默认设为完整邮箱
            user.setUsername(registerDTO.getEmail());
        } else {
            // 手机号注册：用户名默认设为手机号
            user.setUsername(registerDTO.getPhone());
        }
        user.setStatus(1);

        return userRepository.save(user);
    }

    @Override
    public void sendCode(String email, String phone, String type) {
        Assert.notNull(type, "Verification code type cannot be empty.");  // 验证码类型不能为空

        // 确保验证码类型与登录时使用的类型一致
        String actualType = type;
        if ("login".equals(type) || "register".equals(type) || "forgot".equals(type)) {
            actualType = type;
        } else {
            // 默认使用登录类型
            actualType = "login";
            logger.warn("Unsupported code type: {}, using default type: login", type);
        }

        if (email != null && !email.isEmpty()) {
            // 发送邮件验证码
            String code = verificationCodeService.generateCode(email, actualType);
            mailService.sendVerificationCode(email, code, actualType);
        } else if (phone != null && !phone.isEmpty()) {
            // 发送短信验证码
            String code = verificationCodeService.generateCode(phone, actualType);
            smsService.sendVerificationCode(phone, code, actualType);
        } else {
            throw new RuntimeException("Email or phone number cannot be empty.");  // 邮箱或手机号不能为空
        }
    }

    @Override
    public boolean verifyCode(String email, String phone, String code, String type) {
        Assert.notNull(code, "Verification code cannot be empty.");  // 验证码不能为空
        Assert.notNull(type, "Verification code type cannot be empty.");  // 验证码类型不能为空

        // 确保验证码类型与发送时使用的类型一致
        String actualType = type;
        if ("login".equals(type) || "register".equals(type) || "forgot".equals(type)) {
            actualType = type;
        } else {
            // 默认使用登录类型
            actualType = "login";
            logger.warn("Unsupported code type: {}, using default type: login", type);
        }

        if (email != null && !email.isEmpty()) {
            return verificationCodeService.verifyCode(email, code, actualType);
        } else if (phone != null && !phone.isEmpty()) {
            return verificationCodeService.verifyCode(phone, code, actualType);
        } else {
            throw new RuntimeException("Email or phone number cannot be empty.");  // 邮箱或手机号不能为空
        }
    }

    @Override
    public boolean forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        Assert.notNull(forgotPasswordDTO, "Forgot password information cannot be empty.");  // 忘记密码信息不能为空

        // 验证密码一致性
        if (!forgotPasswordDTO.getNewPassword().equals(forgotPasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("The two passwords do not match.");  // 两次输入的密码不一致
        }

        // 查找用户
        User user = null;
        if (forgotPasswordDTO.getEmail() != null && !forgotPasswordDTO.getEmail().isEmpty()) {
            // 邮箱找回
            user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
            
            // 验证验证码
            if (!verificationCodeService.verifyCode(forgotPasswordDTO.getEmail(), forgotPasswordDTO.getCode(), "forgot")) {
                throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
            }
        } else if (forgotPasswordDTO.getPhone() != null && !forgotPasswordDTO.getPhone().isEmpty()) {
            // 手机号找回
            user = userRepository.findByPhone(forgotPasswordDTO.getPhone())
                    .orElseThrow(() -> new RuntimeException("This’s not registered account."));  // 该用户未注册
            
            // 验证验证码
            if (!verificationCodeService.verifyCode(forgotPasswordDTO.getPhone(), forgotPasswordDTO.getCode(), "forgot")) {
                throw new RuntimeException("Verification code is incorrect or has expired.");  // 验证码错误或已过期
            }
        } else {
            throw new RuntimeException("Email or phone number cannot be empty.");  // 邮箱或手机号不能为空
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void logout(String token) {
        // 将token加入黑名单
        jwtUtil.addToBlacklist(token);
        logger.info("用户退出登录，token已加入黑名单: {}", token);
    }
}