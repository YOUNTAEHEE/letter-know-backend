package com.know.letter.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.know.letter.user.command.application.dto.JoinRequestDTO;
import com.know.letter.user.command.application.dto.LoginRequestDTO;
import com.know.letter.user.command.application.dto.LoginResponseDTO;
import com.know.letter.user.command.application.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinRequestDTO joinRequestDTO) {
        userService.join(joinRequestDTO);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO response = userService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }
} 