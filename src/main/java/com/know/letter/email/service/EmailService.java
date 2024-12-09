package com.know.letter.email.service;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    
    public String sendVerificationEmail(String to) throws MessagingException {
        String verificationCode = generateVerificationCode();
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setTo(to);
        helper.setSubject("이메일 인증 코드");
        helper.setText("인증 코드: " + verificationCode);
        
        emailSender.send(message);
        return verificationCode;
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
} 