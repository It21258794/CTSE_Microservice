package com.microservice.authservice.Controller;

import com.microservice.authservice.Services.AuthenticationService;
import com.microservice.authservice.dto.AuthenticationRequest;
import com.microservice.authservice.dto.AuthenticationResponse;
import com.microservice.authservice.dto.PasswordResetRequestDto;
import com.microservice.authservice.dto.PasswordResetVerifyDto;
import com.microservice.authservice.dto.RegisterRequest;
import com.microservice.authservice.dto.UpdatePasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        try {
            authenticationService.updatePassword(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<?> resetPasswordRequest(@RequestBody PasswordResetRequestDto request) {
        try {
            authenticationService.initiatePasswordReset(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password-verify")
    public ResponseEntity<?> resetPasswordVerify(@RequestBody PasswordResetVerifyDto request) {
        try {
            authenticationService.completePasswordReset(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 