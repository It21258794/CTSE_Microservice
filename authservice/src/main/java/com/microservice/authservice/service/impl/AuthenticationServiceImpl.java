package com.microservice.authservice.service.impl;

import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.dto.AuthenticationRequest;
import com.microservice.authservice.dto.AuthenticationResponse;
import com.microservice.authservice.dto.PasswordResetRequestDto;
import com.microservice.authservice.dto.PasswordResetVerifyDto;
import com.microservice.authservice.dto.RegisterRequest;
import com.microservice.authservice.dto.UpdatePasswordRequest;
import com.microservice.authservice.model.Role;
import com.microservice.authservice.model.User;
import com.microservice.authservice.security.JwtService;
import com.microservice.authservice.service.AuthenticationService;
import com.microservice.authservice.service.EmailService;
import com.microservice.authservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpService otpService;

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        System.out.println("Attempting to register user with email: " + request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            System.out.println("Registration failed: Email already exists - " + request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        System.out.println("Creating new user: " + request.getEmail());
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER.name())
                .active(true)
                .build();

        System.out.println("Saving user to database: " + request.getEmail());
        userRepository.save(user);

        System.out.println("Generating JWT token for: " + request.getEmail());
        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void initiatePasswordReset(PasswordResetRequestDto request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendPasswordResetEmail(request.getEmail(), otp);
    }

    @Override
    public void completePasswordReset(PasswordResetVerifyDto request) {
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.invalidateOtp(request.getEmail());
    }
} 