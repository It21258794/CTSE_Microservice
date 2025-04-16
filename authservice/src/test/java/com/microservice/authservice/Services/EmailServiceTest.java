package com.microservice.authservice.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    }

    @Test
    void sendEmail_ShouldSendEmailSuccessfully() {
        // Act
        emailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ShouldThrowException_WhenEmailSendingFails() {
        // Arrange
        doThrow(new RuntimeException("Failed to send email")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            emailService.sendEmail("recipient@example.com", "Test Subject", "Test Body")
        );
    }

    @Test
    void sendOtpEmail_ShouldSendEmailSuccessfully() {
        // Act
        emailService.sendOtpEmail("recipient@example.com", "123456");

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldThrowException_WhenEmailSendingFails() {
        // Arrange
        doThrow(new RuntimeException("Failed to send email")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            emailService.sendOtpEmail("recipient@example.com", "123456")
        );
    }
} 