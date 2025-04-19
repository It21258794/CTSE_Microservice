package com.microservice.authservice.service;

import com.microservice.authservice.dto.AuthenticationRequest;
import com.microservice.authservice.dto.AuthenticationResponse;
import com.microservice.authservice.dto.PasswordResetRequestDto;
import com.microservice.authservice.dto.PasswordResetVerifyDto;
import com.microservice.authservice.dto.RegisterRequest;
import com.microservice.authservice.dto.UpdatePasswordRequest;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void updatePassword(UpdatePasswordRequest request);
    void initiatePasswordReset(PasswordResetRequestDto request);
    void completePasswordReset(PasswordResetVerifyDto request);
} 