package com.know.letter.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.know.letter.user.dto.TokenDTO;
import com.know.letter.user.dto.UserJoinDTO;
import com.know.letter.user.dto.UserLoginDTO;
import com.know.letter.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserJoinDTO userJoinDTO) {
        userService.join(userJoinDTO);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody UserLoginDTO userLoginDTO) {
        TokenDTO tokenDTO = userService.login(userLoginDTO);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokenDTO.getAccessToken())
                .body(tokenDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
            return ResponseEntity.ok(userService.refreshToken(refreshToken));
        }
        return ResponseEntity.badRequest().build();
    }
} 