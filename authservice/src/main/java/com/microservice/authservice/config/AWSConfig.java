package com.microservice.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AWSConfig {
    
    @Value("${aws.access-key}")
    private String accessKey;
    
    @Value("${aws.secret-key}")
    private String secretKey;
    
    @Value("${aws.region}")
    private String region;
    
    @Bean
    public SesClient sesClient() {
        System.out.println("=== AWS Configuration ===");
        System.out.println("Region: " + region);
        System.out.println("Access Key: " + accessKey);
        System.out.println("Secret Key: " + (secretKey != null ? "***" + secretKey.substring(secretKey.length() - 4) : "null"));
        
        // Clean the secret key (remove any potential whitespace or special characters)
        String cleanedSecretKey = secretKey.trim();
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey.trim(), cleanedSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);
        
        return SesClient.builder()
                .region(Region.of(region.trim()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
} 