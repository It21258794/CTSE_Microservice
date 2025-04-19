package com.microservice.authservice.service.impl;

import com.microservice.authservice.dto.AuthenticationRequest;
import com.microservice.authservice.dto.AuthenticationResponse;
import com.microservice.authservice.dto.PasswordResetRequestDto;
import com.microservice.authservice.dto.PasswordResetVerifyDto;
import com.microservice.authservice.dto.RegisterRequest;
import com.microservice.authservice.model.Role;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.security.JwtService;
import com.microservice.authservice.service.EmailService;
import com.microservice.authservice.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER.name())
                .active(true)
                .build();
    }

    @Test
    void register_NewUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_ExistingEmail_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> authenticationService.register(registerRequest));
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ValidCredentials_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void initiatePasswordReset_ValidEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(otpService.generateOtp(anyString())).thenReturn("123456");

        authenticationService.initiatePasswordReset(PasswordResetRequestDto.builder()
                .email("john.doe@example.com")
                .build());

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(otpService).generateOtp("john.doe@example.com");
        verify(emailService).sendPasswordResetEmail("john.doe@example.com", "123456");
    }

    @Test
    void completePasswordReset_ValidOtp_Success() {
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        authenticationService.completePasswordReset(PasswordResetVerifyDto.builder()
                .email("john.doe@example.com")
                .otp("123456")
                .newPassword("newPassword123")
                .build());

        verify(otpService).validateOtp("john.doe@example.com", "123456");
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
        verify(otpService).invalidateOtp("john.doe@example.com");
    }
} 