package com.know.letter.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinDTO {
    private String userId;
    private String userPassword;
    private String email;
    private String name;
} 