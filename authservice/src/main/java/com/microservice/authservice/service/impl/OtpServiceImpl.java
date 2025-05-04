package com.microservice.authservice.service.impl;

import com.microservice.authservice.service.OtpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {
    private final Map<String, String> otpStore = new HashMap<>();
    private final Map<String, Long> otpExpiry = new HashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MINUTES = 5;

    @Override
    public String generateOtp(String email) {
        String otp = generateRandomOtp();
        otpStore.put(email, otp);
        otpExpiry.put(email, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_EXPIRY_MINUTES));
        return otp;
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        String storedOtp = otpStore.get(email);
        Long expiryTime = otpExpiry.get(email);

        if (storedOtp == null || expiryTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiryTime) {
            invalidateOtp(email);
            return false;
        }

        return storedOtp.equals(otp);
    }

    @Override
    public void invalidateOtp(String email) {
        otpStore.remove(email);
        otpExpiry.remove(email);
    }

    private String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
} 