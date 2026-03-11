package com.cgnpc.drm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.AssertTrue;
import java.util.Objects;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$", 
             message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 验证至少提供了邮箱或手机号中的一个
     */
    @AssertTrue(message = "邮箱或手机号至少提供一个")
    public boolean isValidRegisterType() {
        return (email != null && !email.isEmpty()) || (phone != null && !phone.isEmpty());
    }

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}