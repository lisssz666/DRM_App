package com.cgnpc.drm.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import javax.annotation.PostConstruct;

/**
 * 验证码服务
 */
@Service
public class VerificationCodeService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);

    /**
     * 存储验证码信息的缓存
     * key: 邮箱/手机号 + 类型
     * value: Map包含验证码和过期时间
     */
    private final Map<String, Map<String, Object>> codeCache = new ConcurrentHashMap<>();
    
    /**
     * 应用程序启动时清除所有缓存
     */
    @PostConstruct
    public void clearCacheOnStartup() {
        codeCache.clear();
        logger.info("Cleared all verification code cache on application startup");
    }

    /**
     * 验证码有效时间（分钟）
     */
    private static final int CODE_EXPIRATION_MINUTES = 5;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 4;

    /**
     * 生成验证码
     * @param key 邮箱或手机号
     * @param type 用途类型
     * @return 验证码
     */
    public String generateCode(String key, String type) {
        // 对key参数进行修剪操作，去除空格和其他不可见字符
        key = key.trim();
        // 确保type参数不为空或null
        if (type == null || type.isEmpty()) {
            type = "login";
            logger.warn("Empty code type, using default type: login");
        }
        String cacheKey = buildCacheKey(key, type);
        String code = generateRandomCode();

        Map<String, Object> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        codeInfo.put("expireTime", expireTime);

        codeCache.put(cacheKey, codeInfo);

        logger.info("Generated verification code - cacheKey: {}, key: {}, maskedKey: {}, type: {}, code: {}, expires at: {}", 
                   cacheKey, key, maskKey(key), type, code, expireTime);
        // 打印生成的验证码到控制台，方便调试
        System.out.println("【调试】生成验证码：" + code + "，手机号/邮箱：" + maskKey(key) + "，类型：" + type);

        return code;
    }

    /**
     * 验证验证码
     * @param key 邮箱或手机号
     * @param code 输入的验证码
     * @param type 用途类型
     * @return 是否验证成功
     */
    public boolean verifyCode(String key, String code, String type) {
        // 对key参数进行修剪操作，去除空格和其他不可见字符
        key = key.trim();
        // 确保type参数不为空或null，与generateCode方法保持一致
        if (type == null || type.isEmpty()) {
            type = "login";
            logger.warn("Empty code type during verification, using default type: login");
        }
        String cacheKey = buildCacheKey(key, type);
        Map<String, Object> codeInfo = codeCache.get(cacheKey);

        if (codeInfo == null) {
            logger.info("Verification code not found - cacheKey: {}, key: {}, maskedKey: {}, type: {}", 
                       cacheKey, key, maskKey(key), type);
            logger.info("Current cache size: {}, cache contents: {}", codeCache.size(), codeCache.keySet());
            return false;
        }

        String storedCode = (String) codeInfo.get("code");
        LocalDateTime expireTime = (LocalDateTime) codeInfo.get("expireTime");

        // 检查验证码是否已过期
        if (LocalDateTime.now().isAfter(expireTime)) {
            codeCache.remove(cacheKey);
            logger.info("Verification code expired for key: {}, type: {}, expired at: {}", 
                       maskKey(key), type, expireTime);
            return false;
        }

        // 验证验证码是否匹配
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码
            codeCache.remove(cacheKey);
            logger.info("Verification code matched for key: {}, type: {}", maskKey(key), type);
        } else {
            logger.info("Verification code mismatch for key: {}, type: {}", maskKey(key), type);
        }

        return isValid;
    }

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 掩码敏感信息（邮箱或手机号）
     * @param key 邮箱或手机号
     * @return 掩码后的字符串
     */
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        if (key.contains("@")) {
            // 邮箱掩码：保留前两位和域名
            int atIndex = key.indexOf('@');
            if (atIndex > 2) {
                return key.substring(0, 2) + "****" + key.substring(atIndex);
            }
            return key;
        } else {
            // 手机号掩码：保留前三位和后四位
            if (key.length() > 7) {
                return key.substring(0, 3) + "****" + key.substring(key.length() - 4);
            }
            return key;
        }
    }

    /**
     * 生成随机验证码
     * @return 4位数字验证码
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 构建缓存键
     * @param key 邮箱或手机号
     * @param type 用途类型
     * @return 缓存键
     */
    private String buildCacheKey(String key, String type) {
        return key + "_" + type;
    }

    /**
     * 清理过期的验证码
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = codeCache.size();
        
        codeCache.entrySet().removeIf(entry -> {
            String cacheKey = entry.getKey();
            // 移除所有没有"_"的缓存键（格式不正确的缓存键）
            if (!cacheKey.contains("_")) {
                logger.warn("Removing invalid cache key: {}", cacheKey);
                return true;
            }
            // 移除过期的验证码
            LocalDateTime expireTime = (LocalDateTime) entry.getValue().get("expireTime");
            return now.isAfter(expireTime);
        });
        
        int afterSize = codeCache.size();
        int removedCount = beforeSize - afterSize;
        if (removedCount > 0) {
            logger.info("Cleaned {} expired verification codes, current cache size: {}", removedCount, afterSize);
        }
    }
}