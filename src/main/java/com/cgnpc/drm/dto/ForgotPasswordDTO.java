package com.cgnpc.drm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 忘记密码DTO
 */
@Data
public class ForgotPasswordDTO {

    /**
     * 邮箱（用于接收验证码）
     */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$", 
             message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;
}