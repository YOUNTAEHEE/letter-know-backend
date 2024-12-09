package com.know.letter.email.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.know.letter.email.dto.EmailVerificationDTO;
import com.know.letter.email.dto.EmailVerificationRequestDTO;
import com.know.letter.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailVerificationRequestDTO request) {
        try {
            String verificationCode = emailService.sendVerificationEmail(request.getEmail());
            // Store the verification code in Redis with 5 minutes expiration
            redisTemplate.opsForValue().set(
                "email:verification:" + request.getEmail(),
                verificationCode,
                5,
                TimeUnit.MINUTES
            );
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("이메일 발송에 실패했습니다.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationDTO verificationDTO) {
        String storedCode = redisTemplate.opsForValue().get("email:verification:" + verificationDTO.getEmail());
        
        if (storedCode == null) {
            return ResponseEntity.badRequest().body("인증 코드가 만료되었습니다.");
        }
        
        if (storedCode.equals(verificationDTO.getCode())) {
            redisTemplate.delete("email:verification:" + verificationDTO.getEmail());
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 일치하지 않습니다.");
        }
    }
} 