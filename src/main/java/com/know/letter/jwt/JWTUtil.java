package com.know.letter.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.know.letter.user.command.domain.aggregate.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JWTUtil(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access-token-expiration}") Long accessTokenExpiration,
            @Value("${spring.jwt.refresh-token-expiration}") Long refreshTokenExpiration
    ) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), 
                                         Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getUserId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", String.class);
    }

    public UserRole getRole(String token) {
        String roleName = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        return UserRole.valueOf(roleName);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String createAccessToken(String userId, UserRole role) {
        return createToken(userId, role, accessTokenExpiration);
    }

    public String createRefreshToken(String userId, UserRole role) {
        return createToken(userId, role, refreshTokenExpiration);
    }


    public String createToken(String userId, UserRole role, Long expiredMs) {
        try {
            String token = Jwts.builder()
                    .claim("userId", userId)
                    .claim("role", role.name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

            System.out.println("Generated Token: " + token); // 로그 추가
            return token;
        } catch (Exception e) {
            System.out.println("JWT creation failed: " + e.getMessage()); // 오류 발생 시 로그
            return null;
        }
    }

    public Long getRefreshTokenExpiration() {
        return this.refreshTokenExpiration;
    }

    public Boolean validateToken(String token) {
        try {
            // 토큰 파싱 시도
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            
            // 만료 여부 확인
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}