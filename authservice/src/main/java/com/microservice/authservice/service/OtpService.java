package com.microservice.authservice.service;

public interface OtpService {
    String generateOtp(String email);
    boolean validateOtp(String email, String otp);
    void invalidateOtp(String email);
} 