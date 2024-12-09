package com.know.letter.jwt;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refreshToken")
@Builder
public class RefreshToken {
    @Id
    private String userId;
    private String refreshToken;
    
    @TimeToLive
    private Long expiration; // seconds
} 