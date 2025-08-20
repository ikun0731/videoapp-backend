package com.example.videoapp;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT工具类，用于生成和解析JWT令牌
 */
@Component
public class JwtUtil {
    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret}")
    private String secretKey;
    
    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private long expiration;
    
    /**
     * 生成JWT令牌
     * 
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public String generateToken(String username) {
        Date currentTime = new Date(System.currentTimeMillis());
        Date expirationTime = new Date(System.currentTimeMillis() + expiration);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentTime)
                .setExpiration(expirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        return claims.getSubject();
    }
}
