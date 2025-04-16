package com.microservice.authservice.Services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {
    private final Map<String, OtpData> otpMap = new HashMap<>();
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes

    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpMap.put(email, new OtpData(otp, System.currentTimeMillis()));
        return otp;
    }

    public boolean validateOTP(String email, String otp) {
        OtpData otpData = otpMap.get(email);
        if (otpData == null) {
            return false;
        }

        // Check if OTP is expired
        if (System.currentTimeMillis() - otpData.timestamp > OTP_VALID_DURATION) {
            otpMap.remove(email);
            return false;
        }

        // Check if OTP matches
        if (otpData.otp.equals(otp)) {
            otpMap.remove(email);
            return true;
        }
        return false;
    }

    private static class OtpData {
        String otp;
        long timestamp;

        OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
} 