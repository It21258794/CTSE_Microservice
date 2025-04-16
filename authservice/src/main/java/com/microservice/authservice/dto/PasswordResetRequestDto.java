package com.microservice.authservice.dto;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String email;
} 