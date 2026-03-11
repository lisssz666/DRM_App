package com.cgnpc.drm.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 */
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证邮件
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @param type 类型：login(登录), register(注册), forgot(忘记密码)
     */
    public void sendVerificationCode(String toEmail, String code, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);

        String subject;
        String text;

        switch (type) {
            case "login":
                subject = "【DRM系统】登录验证码";
                text = "您的登录验证码是：" + code + "，验证码有效期为5分钟。";
                break;
            case "register":
                subject = "【DRM系统】注册验证码";
                text = "您的注册验证码是：" + code + "，验证码有效期为5分钟。";
                break;
            case "forgot":
                subject = "【DRM系统】重置密码验证码";
                text = "您的重置密码验证码是：" + code + "，验证码有效期为5分钟。";
                break;
            default:
                subject = "【DRM系统】验证码";
                text = "您的验证码是：" + code + "，验证码有效期为5分钟。";
        }

        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}