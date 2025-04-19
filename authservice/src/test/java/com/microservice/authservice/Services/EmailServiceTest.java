package com.microservice.authservice.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendEmail_Success() {
        // Arrange
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(toEmail, subject, body);

        // Assert
        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmail_Failure() {
        // Arrange
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("Failed to send email").build());

        // Act & Assert
        try {
            emailService.sendEmail(toEmail, subject, body);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendOtpEmail_Success() {
        // Arrange
        String toEmail = "test@example.com";
        String otp = "123456";
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        emailService.sendOtpEmail(toEmail, otp);

        // Assert
        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendOtpEmail_Failure() {
        // Arrange
        String toEmail = "test@example.com";
        String otp = "123456";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("Failed to send email").build());

        // Act & Assert
        try {
            emailService.sendOtpEmail(toEmail, otp);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }
} 