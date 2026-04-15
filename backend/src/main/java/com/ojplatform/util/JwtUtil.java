package com.ojplatform.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        return JWT.create()
                .withIssuer("oj-platform")
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role != null ? role : "user")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证并解析 JWT Token
     *
     * @param token JWT 字符串
     * @return 解码后的 JWT 对象
     */
    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("oj-platform")
                .build();
        return verifier.verify(token);
    }

    /**
     * 从 Token 中提取用户 ID
     */
    public Long getUserId(String token) {
        return verifyToken(token).getClaim("userId").asLong();
    }

    /**
     * 从 Token 中提取用户名
     */
    public String getUsername(String token) {
        return verifyToken(token).getClaim("username").asString();
    }

    /**
     * 从 Token 中提取角色
     */
    public String getRole(String token) {
        return verifyToken(token).getClaim("role").asString();
    }
}
