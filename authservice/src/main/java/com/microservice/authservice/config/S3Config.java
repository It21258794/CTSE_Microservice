package com.microservice.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        System.out.println("=== S3 Configuration ===");
        System.out.println("Region: " + region);
        System.out.println("Access Key: " + accessKey);
        System.out.println("Secret Key: " + (secretKey != null ? "***" + secretKey.substring(secretKey.length() - 4) : "null"));
        
        // Clean the secret key (remove any potential whitespace or special characters)
        String cleanedSecretKey = secretKey.trim();
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey.trim(), cleanedSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);
        
        return S3Client.builder()
                .region(Region.of(region.trim()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}