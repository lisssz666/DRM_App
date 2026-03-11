package com.cgnpc.drm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {

    /**
     * 邮箱（用于邮箱登录）
     */
    private String email;

    /**
     * 手机号（用于手机登录）
     */
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码（用于验证码登录）
     */
    private String code;

    /**
     * 登录类型：password(密码登录), code(验证码登录)
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
}