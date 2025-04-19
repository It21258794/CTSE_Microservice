package com.microservice.authservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceImplTest {

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl();
    }

    @Test
    void generateOtp_ValidEmail_ReturnsOtp() {
        String otp = otpService.generateOtp("test@example.com");
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d+"));
    }

    @Test
    void validateOtp_ValidOtp_ReturnsTrue() {
        String email = "test@example.com";
        String otp = otpService.generateOtp(email);
        boolean isValid = otpService.validateOtp(email, otp);
        assertTrue(isValid);
    }

    @Test
    void validateOtp_InvalidOtp_ReturnsFalse() {
        String email = "test@example.com";
        otpService.generateOtp(email);
        boolean isValid = otpService.validateOtp(email, "000000");
        assertFalse(isValid);
    }

    @Test
    void validateOtp_ExpiredOtp_ReturnsFalse() throws InterruptedException {
        String email = "test@example.com";
        String otp = otpService.generateOtp(email);
        Thread.sleep(6 * 60 * 1000);
        boolean isValid = otpService.validateOtp(email, otp);
        assertFalse(isValid);
    }

    @Test
    void invalidateOtp_ValidEmail_RemovesOtp() {
        String email = "test@example.com";
        String otp = otpService.generateOtp(email);
        otpService.invalidateOtp(email);
        assertFalse(otpService.validateOtp(email, otp));
    }

    @Test
    void generateOtp_DifferentEmails_ReturnsDifferentOtps() {
        String otp1 = otpService.generateOtp("test1@example.com");
        String otp2 = otpService.generateOtp("test2@example.com");
        assertNotEquals(otp1, otp2);
    }
} 