package com.microservice.authservice.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private final String apiToken;
    private final String fromEmail;
    private final String apiUrl = "https://api.postmarkapp.com/email";

    public EmailService(
            @Value("${postmark.api.token}") String apiToken,
            @Value("${postmark.sender.email}") String fromEmail) {
        this.restTemplate = new RestTemplate();
        this.apiToken = apiToken;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        System.out.println("=== Starting Email Send Process ===");
        System.out.println("Configuration Details:");
        System.out.println("- API URL: " + apiUrl);
        System.out.println("- Using API Token: " + apiToken);
        
        System.out.println("\nEmail Details:");
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-Postmark-Server-Token", apiToken);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("From", fromEmail);
        emailData.put("To", toEmail);
        emailData.put("Subject", subject);
        emailData.put("TextBody", body);
        emailData.put("MessageStream", "outbound");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

        try {
            System.out.println("\nAttempting to send email...");
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            System.out.println("Email sent successfully!");
            System.out.println("Response: " + response);
            System.out.println("=== Email Send Process Complete ===");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            System.err.println("Stack trace: ");
            e.printStackTrace();
            throw new RuntimeException("Failed to send email. Please check the configuration and try again.", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail, "Password Reset OTP", 
            "Your OTP for password reset is: " + otp + "\nThis OTP will expire in 5 minutes.");
    }
} 