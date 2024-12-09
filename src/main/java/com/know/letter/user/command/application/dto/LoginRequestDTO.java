package com.know.letter.user.command.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    private String userId;
    private String userPassword;
} 