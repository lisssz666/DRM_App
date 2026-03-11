package com.cgnpc.drm.service;

import com.cgnpc.drm.dto.ForgotPasswordDTO;
import com.cgnpc.drm.dto.UserLoginDTO;
import com.cgnpc.drm.dto.UserRegisterDTO;
import com.cgnpc.drm.entity.User;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录（密码登录或验证码登录）
     * @param loginDTO 登录信息
     * @return 登录成功的用户信息
     */
    User login(UserLoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册成功的用户信息
     */
    User register(UserRegisterDTO registerDTO);

    /**
     * 发送验证码（用于登录或注册）
     * @param email 邮箱
     * @param phone 手机号
     * @param type 用途类型：login(登录), register(注册), forgot(忘记密码)
     */
    void sendCode(String email, String phone, String type);

    /**
     * 验证验证码
     * @param email 邮箱
     * @param phone 手机号
     * @param code 验证码
     * @param type 用途类型：login(登录), register(注册), forgot(忘记密码)
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String phone, String code, String type);

    /**
     * 忘记密码，重置密码
     * @param forgotPasswordDTO 忘记密码信息
     * @return 重置密码是否成功
     */
    boolean forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    User findByEmail(String email);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(String phone);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 用户退出登录
     * @param token 用户token
     */
    void logout(String token);
}