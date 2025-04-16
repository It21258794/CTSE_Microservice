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
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/update-password")
    public ResponseEntity<Void> updatePassword(
            @RequestBody UpdatePasswordRequest request
    ) {
        authenticationService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<Void> resetPasswordRequest(
            @RequestBody PasswordResetRequestDto request
    ) {
        authenticationService.initiatePasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password-verify")
    public ResponseEntity<Void> resetPasswordVerify(
            @RequestBody PasswordResetVerifyDto request
    ) {
        authenticationService.completePasswordReset(request);
        return ResponseEntity.ok().build();
    }
} 