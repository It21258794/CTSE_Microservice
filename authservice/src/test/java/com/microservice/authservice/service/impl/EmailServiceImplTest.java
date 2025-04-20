package com.microservice.authservice.service.impl;

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

class EmailServiceImplTest {
    @Mock
    private SesClient sesClient;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendEmail_Success() {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        emailService.sendEmail(toEmail, subject, body);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmail_Failure() {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("Failed to send email").build());

        try {
            emailService.sendEmail(toEmail, subject, body);
        } catch (RuntimeException e) {
        }

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        String toEmail = "test@example.com";
        String otp = "123456";
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        emailService.sendPasswordResetEmail(toEmail, otp);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendPasswordResetEmail_Failure() {
        String toEmail = "test@example.com";
        String otp = "123456";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("Failed to send email").build());

        try {
            emailService.sendPasswordResetEmail(toEmail, otp);
        } catch (RuntimeException e) {
        }

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendWelcomeEmail_Success() {
        String toEmail = "test@example.com";
        String name = "John Doe";
        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        emailService.sendWelcomeEmail(toEmail, name);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendWelcomeEmail_Failure() {
        String toEmail = "test@example.com";
        String name = "John Doe";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("Failed to send email").build());

        try {
            emailService.sendWelcomeEmail(toEmail, name);
        } catch (RuntimeException e) {
        }

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }
} 