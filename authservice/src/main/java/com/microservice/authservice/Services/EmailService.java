package com.microservice.authservice.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    private final SesClient sesClient;
    private final String sourceEmail;

    public EmailService(
            SesClient sesClient,
            @Value("${aws.ses.source-email}") String sourceEmail) {
        this.sesClient = sesClient;
        this.sourceEmail = sourceEmail;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            System.out.println("\n=== Starting Email Send Process ===");
            System.out.println("AWS SES Configuration:");
            System.out.println("- Source Email: " + sourceEmail);
            System.out.println("- Recipient Email: " + toEmail);
            System.out.println("- Subject: " + subject);

            // Build the email request
            Destination destination = Destination.builder()
                    .toAddresses(toEmail)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();

            Content bodyContent = Content.builder()
                    .data(body)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(Body.builder().text(bodyContent).build())
                    .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(sourceEmail)
                    .build();

            System.out.println("\nSending email through AWS SES...");
            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            
            System.out.println("\nEmail sent successfully!");
            System.out.println("Message ID: " + response.messageId());
            System.out.println("Request ID: " + response.responseMetadata().requestId());
            System.out.println("=== Email Send Process Complete ===\n");

        } catch (SesException e) {
            System.err.println("\n=== Email Send Failed ===");
            System.err.println("Error Code: " + e.awsErrorDetails().errorCode());
            System.err.println("Error Message: " + e.awsErrorDetails().errorMessage());
            System.err.println("Request ID: " + e.requestId());
            System.err.println("Status Code: " + e.statusCode());
            throw new RuntimeException("Failed to send email through AWS SES", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail, "Password Reset OTP",
                "Your OTP for password reset is: " + otp + "\nThis OTP will expire in 5 minutes.");
    }
} 