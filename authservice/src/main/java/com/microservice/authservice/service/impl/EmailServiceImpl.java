package com.microservice.authservice.service.impl;

import com.microservice.authservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.source-email}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            System.out.println("Attempting to send email from: " + fromEmail + " to: " + to);
            
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(content).build()).build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            System.out.println("Email sent successfully. Message ID: " + response.messageId());
        } catch (SesException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            System.err.println("Error code: " + e.awsErrorDetails().errorCode());
            System.err.println("Error message: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send email: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error while sending email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error while sending email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String otp) {
        String subject = "Password Reset Request";
        String content = "Your OTP for password reset is: " + otp + "\nThis OTP will expire in 5 minutes.";
        sendEmail(to, subject, content);
    }

    @Override
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Our Platform";
        String content = "Dear " + name + ",\n\nWelcome to our platform! We're excited to have you on board.";
        sendEmail(to, subject, content);
    }
}
