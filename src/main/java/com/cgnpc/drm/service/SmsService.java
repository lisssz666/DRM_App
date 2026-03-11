package com.cgnpc.drm.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信发送服务（阿里云短信服务实现）
 */
@Service
public class SmsService {

    /**
     * 创建阿里云短信服务客户端
     * @return 阿里云短信服务客户端
     * @throws Exception 创建客户端异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                // 配置 AccessKey ID，请确保代码运行环境设置了环境变量
                .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                // 配置 AccessKey Secret，请确保代码运行环境设置了环境变量
                .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        
        // 配置 Endpoint
        config.endpoint = "dysmsapi.aliyuncs.com";
        
        return new Client(config);
    }

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @param type 类型：login(登录), register(注册), forgot(忘记密码)
     */
    public void sendVerificationCode(String phone, String code, String type) {
        try {
            // 初始化请求客户端
            Client client = createClient();
            
            // 构造短信模板参数
            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("code", code);
            String templateParamJson = Common.toJSONString(templateParams);
            
            // 构造请求对象
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName("迪而美深圳日用品") // 请替换为实际的短信签名
                    .setTemplateCode(getTemplateCode(type)) // 根据类型获取模板ID
                    .setTemplateParam(templateParamJson);
            
            // 发送短信
            SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
            
            // 输出响应结果（实际项目中建议记录日志）
            System.out.println("【DRM系统】阿里云短信发送结果：");
            System.out.println("手机号: " + phone);
            System.out.println("请求ID: " + sendSmsResponse.getBody().getRequestId());
            System.out.println("状态码: " + sendSmsResponse.getBody().getCode());
            System.out.println("状态消息: " + sendSmsResponse.getBody().getMessage());
            
            // 检查发送是否成功
            if (!"OK".equals(sendSmsResponse.getBody().getCode())) {
                throw new RuntimeException("SMS sending failed: " + sendSmsResponse.getBody().getMessage());  // 短信发送失败
            }
            
        } catch (Exception e) {
            // 实际项目中建议记录详细日志
            System.err.println("【DRM系统】短信发送失败：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("SMS sending failed: " + e.getMessage());  // 短信发送失败
        }
    }

    /**
     * 根据类型获取短信模板ID
     * @param type 类型：login(登录), register(注册), forgot(忘记密码)
     * @return 短信模板ID
     */
    private String getTemplateCode(String type) {
        // 请替换为实际的短信模板ID
        switch (type) {
            case "login":
                return "SMS_501280118";
            case "register":
                return "SMS_501280118";
            case "forgot":
                return "SMS_501280118";
            default:
                return "SMS_501280118";
        }
    }
}