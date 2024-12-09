package com.know.letter.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.know.letter.jwt.JWTUtil;
import com.know.letter.jwt.RefreshToken;
import com.know.letter.jwt.RefreshTokenRepository;
import com.know.letter.user.command.domain.aggregate.User;
import com.know.letter.user.command.domain.aggregate.UserRole;
import com.know.letter.user.dto.TokenDTO;
import com.know.letter.user.dto.UserJoinDTO;
import com.know.letter.user.dto.UserLoginDTO;
import com.know.letter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void join(UserJoinDTO userJoinDTO) {
        try {
            if (userJoinDTO == null || userJoinDTO.getUserId() == null || userJoinDTO.getUserPassword() == null) {
                throw new IllegalArgumentException("유효하지 않은 사용자 정보입니다.");
            }

            if (userRepository.existsByUserId(userJoinDTO.getUserId())) {
                throw new IllegalStateException("이미 존재하는 아이디입니다.");
            }

            User user = User.builder()
                    .userId(userJoinDTO.getUserId())
                    .userPassword(passwordEncoder.encode(userJoinDTO.getUserPassword()))
                    .userRole(UserRole.ROLE_USER)
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public TokenDTO login(UserLoginDTO userLoginDTO) {
        try {
            if (userLoginDTO == null || userLoginDTO.getUserId() == null || userLoginDTO.getUserPassword() == null) {
                throw new IllegalArgumentException("로그인 정보가 올바르지 않습니다.");
            }

            User user = userRepository.findByUserId(userLoginDTO.getUserId())
                    .orElseThrow(() -> new IllegalStateException("존재하지 않는 아이디입니다."));

            if (!passwordEncoder.matches(userLoginDTO.getUserPassword(), user.getUserPassword())) {
                throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
            }

            // 기존 리프레시 토큰이 있다면 삭제
            refreshTokenRepository.findById(user.getUserId())
                    .ifPresent(token -> refreshTokenRepository.delete(token));

            String accessToken = jwtUtil.createToken(user.getUserId(), user.getUserRole(), 60 * 60 * 10L);
            String refreshToken = jwtUtil.createRefreshToken(user.getUserId(), user.getUserRole());

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .userId(user.getUserId())
                    .refreshToken(refreshToken)
                    .expiration(jwtUtil.getRefreshTokenExpiration())
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenDTO(accessToken, refreshToken);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public TokenDTO refreshToken(String refreshToken) {
        try {
            // 리프레시 토큰 유효성 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new IllegalStateException("유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰에서 사용자 ID 추출
            String userId = jwtUtil.getUserId(refreshToken);
            
            // DB에 저장된 리프레시 토큰 조회
            RefreshToken storedToken = refreshTokenRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("저장된 리프레시 토큰이 없습니다."));
            
            // 전달받은 토큰과 저장된 토큰 비교
            if (!storedToken.getRefreshToken().equals(refreshToken)) {
                throw new IllegalStateException("리프레시 토큰이 일치하지 않습니다.");
            }

            // 사용자 정보 조회
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자입니다."));

            // 새로운 액세스 토큰 발급
            String newAccessToken = jwtUtil.createToken(userId, user.getUserRole(), 60 * 60 * 10L);
            
            // 새로운 리프레시 토큰 발급
            String newRefreshToken = jwtUtil.createRefreshToken(userId, user.getUserRole());

            // 새로운 리프레시 토큰 저장
            RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                    .userId(userId)
                    .refreshToken(newRefreshToken)
                    .expiration(jwtUtil.getRefreshTokenExpiration())
                    .build();

            refreshTokenRepository.save(newRefreshTokenEntity);

            return new TokenDTO(newAccessToken, newRefreshToken);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("토큰 갱신 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 