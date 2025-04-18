package com.microservice.authservice.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendPasswordResetEmail(String to, String otp);
    void sendWelcomeEmail(String to, String name);
} 