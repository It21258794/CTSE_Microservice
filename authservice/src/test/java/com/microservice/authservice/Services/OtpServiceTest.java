package com.microservice.authservice.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @Test
    void generateOTP_ShouldGenerateValidOTP() {
        String email = "test@example.com";

        String otp = otpService.generateOTP(email);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void validateOTP_ShouldReturnTrue_ForValidOTP() {
        String email = "test@example.com";
        String otp = otpService.generateOTP(email);

        boolean isValid = otpService.validateOTP(email, otp);

        assertTrue(isValid);
    }

    @Test
    void validateOTP_ShouldReturnFalse_ForInvalidOTP() {
        String email = "test@example.com";
        otpService.generateOTP(email);

        boolean isValid = otpService.validateOTP(email, "000000");

        assertFalse(isValid);
    }

    @Test
    void validateOTP_ShouldReturnFalse_ForNonExistentEmail() {
        boolean isValid = otpService.validateOTP("nonexistent@example.com", "123456");

        assertFalse(isValid);
    }

    @Test
    void validateOTP_ShouldReturnFalse_ForExpiredOTP() throws InterruptedException {
        String email = "test@example.com";
        String otp = otpService.generateOTP(email);

        Thread.sleep(5 * 60 * 1000 + 1000);

        boolean isValid = otpService.validateOTP(email, otp);

        assertFalse(isValid);
    }
} 