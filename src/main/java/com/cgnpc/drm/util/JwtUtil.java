package com.cgnpc.drm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    // 密钥，用于签名 JWT（HS256 算法要求密钥长度至少 256 位）
    @Value("${jwt.secret:thisIsASecureSecretKeyForJWTTokenWithAtLeast32Bytes}")
    private String secret;

    // token 有效期，默认 30 天
    @Value("${jwt.expiration:2592000000}")
    private long expiration;

    // 缓存密钥对象，避免重复创建
    private SecretKey signingKey;
    
    // token黑名单，用于存储已失效的token
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    /**
     * 从 token 中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 token 中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从 token 中提取声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 获取签名密钥对象
     */
    private SecretKey getSigningKey() {
        if (signingKey == null) {
            // 使用 Keys.hmacShaKeyFor 方法将字符串密钥转换为符合 HS256 要求的密钥
            // 确保密钥长度至少为 256 位（32 字节）
            signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        }
        return signingKey;
    }

    /**
     * 从 token 中提取所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查 token 是否过期
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成 token
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 创建 token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 token
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token) && !isTokenBlacklisted(token));
    }

    /**
     * 刷新 token
     */
    public String refreshToken(String token) {
        final Claims claims = extractAllClaims(token);
        return createToken(new HashMap<>(claims), claims.getSubject());
    }

    /**
     * 获取 token 剩余有效期（毫秒）
     */
    public long getRemainingTime(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }

    /**
     * 判断 token 是否需要刷新（例如：剩余有效期少于 7 天）
     * 这里可以根据业务需求调整阈值，例如剩余 1 天、3 天等
     */
    public boolean isTokenNeedRefresh(String token) {
        // 计算剩余有效期（毫秒）
        long remainingTime = getRemainingTime(token);
        // 30 天的有效期，当剩余有效期少于 7 天（604800000 毫秒）时需要刷新
        long refreshThreshold = 7 * 24 * 60 * 60 * 1000L;
        return remainingTime < refreshThreshold;
    }
    
    /**
     * 将 token 加入黑名单
     */
    public void addToBlacklist(String token) {
        blacklist.add(token);
    }
    
    /**
     * 检查 token 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
    
    /**
     * 从黑名单中清除过期的 token
     */
    public void cleanBlacklist() {
        blacklist.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                // 如果 token 无效，直接从黑名单中移除
                return true;
            }
        });
    }
}
