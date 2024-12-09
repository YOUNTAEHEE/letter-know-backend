package com.know.letter.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationDTO {
    private String email;
    private String code;
} 