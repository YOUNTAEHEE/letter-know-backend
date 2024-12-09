package com.know.letter.user.command.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequestDTO {
    private String userId;
    private String userPassword;
    private String userName;
    private String email;
} 