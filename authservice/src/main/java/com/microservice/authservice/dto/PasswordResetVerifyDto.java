package com.microservice.authservice.dto;

import lombok.Data;

@Data
public class PasswordResetVerifyDto {
    private String email;
    private String otp;
    private String newPassword;
} 