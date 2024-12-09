package com.know.letter.user.command.application.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.know.letter.jwt.JWTUtil;
import com.know.letter.user.command.application.dto.JoinRequestDTO;
import com.know.letter.user.command.application.dto.LoginRequestDTO;
import com.know.letter.user.command.application.dto.LoginResponseDTO;
import com.know.letter.user.command.domain.aggregate.User;
import com.know.letter.user.command.domain.aggregate.UserRole;
import com.know.letter.user.command.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Transactional
    public void join(JoinRequestDTO joinRequestDTO) {
        if (userRepository.existsByUserId(joinRequestDTO.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByEmail(joinRequestDTO.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setUserId(joinRequestDTO.getUserId());
        user.setUserPassword(passwordEncoder.encode(joinRequestDTO.getUserPassword()));
        user.setUserName(joinRequestDTO.getUserName());
        user.setEmail(joinRequestDTO.getEmail());
        user.setUserRole(UserRole.ROLE_USER);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByUserId(loginRequestDTO.getUserId())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(loginRequestDTO.getUserPassword(), user.getUserPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getUserId(), user.getUserRole(), 60 * 60 * 1000L);
        return new LoginResponseDTO(token, user.getUserId(), user.getUserName());
    }
} 