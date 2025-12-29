package com.coffee.common.util;

import com.coffee.common.constant.AuthConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    // 1. 定义一个辅助方法，把字符串密钥转成 SecretKey 对象
    private SecretKey getSignInKey() {
        // 注意：你的 JWT_SECRET 必须足够长（至少32个字符），否则这里会报错！
        // 如果你的密钥很短，建议随便在常量里加长一点
        byte[] keyBytes = AuthConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 2. 修改生成 Token 的方法
    public String generateToken(Long userId,String phone,Long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);

        return Jwts.builder()
                .claims(claims) // 以前是 setClaims，现在直接用 claims
                .subject(userId.toString()) // 以前是 setSubject，现在也可以用 subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), Jwts.SIG.HS512) // 【重点变化】这里传 Key 对象，算法参数也变了
                .compact();
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId,String phone) {
        return generateToken(userId, phone, AuthConstants.JWT_EXPIRATION);
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId,String phone) {
        Map<String, Object> claims = new HashMap<>();
        // 可以在 claims 中加一个标记，区分这是 refresh token
        claims.put("type", "refresh");
        return generateToken(userId, phone, AuthConstants.REFRESH_EXPIRATION);
    }

    /**
     * 验证Token是否有效
     * @param token
     * @return true/false
     */
    public boolean validateToken(String token) {
        try {
            // 尝试解析 Token
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);

            // 如果上面这行代码没有报错，说明：
            // 1. 签名正确
            // 2. Token 没有过期 (如果过期会抛 ExpiredJwtException)
            return true;
        } catch (ExpiredJwtException e) {
            // 专门捕获过期异常
            log.error("Token 已过期");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // 捕获其他 Token 错误（如签名被篡改、格式错误等）
            log.error("Token 无效");
            return false;
        }
    }

    /**
     * 解析JWT Token获取Claims
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // 这里调用之前写的 helper 方法获取 Key
                .build()
                .parseSignedClaims(token)
                .getPayload(); // 【重点】以前是 getBody()，现在是 getPayload()
    }

    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        // 利用上面的通用方法拿到所有数据
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getPhoneFromToken(String token) {
        return parseToken(token).get("phone", String.class);
    }

}