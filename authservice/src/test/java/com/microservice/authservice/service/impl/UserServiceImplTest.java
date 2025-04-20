package com.microservice.authservice.service.impl;

import com.microservice.authservice.dto.UserDTO;
import com.microservice.authservice.dto.UserResponseDTO;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role("USER")
                .active(true)
                .profilePictureUrl("http://example.com/profile.jpg")
                .build();

        userDTO = UserDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("newPassword")
                .role("USER")
                .active(true)
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("USER")
                .active(true)
                .profilePictureUrl("http://example.com/profile.jpg")
                .build();
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<UserResponseDTO> result = userService.getAllUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        UserResponseDTO result = userService.getUserById("1");
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserById("1"));
    }

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");
        when(userRepository.save(any())).thenReturn(user);

        UserResponseDTO result = userService.updateUser("1", userDTO);
        assertNotNull(result);
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(passwordEncoder).encode(userDTO.getPassword());
    }

    @Test
    void updateUser_NonExistingUser_ThrowsException() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUser("1", userDTO));
    }

    @Test
    void deleteUser_ExistingUser_DeletesSuccessfully() {
        when(userRepository.existsById("1")).thenReturn(true);
        doNothing().when(userRepository).deleteById("1");
        assertDoesNotThrow(() -> userService.deleteUser("1"));
        verify(userRepository).deleteById("1");
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsException() {
        when(userRepository.existsById("1")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> userService.deleteUser("1"));
    }

    @Test
    void toggleUserStatus_ExistingUser_TogglesStatus() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        UserResponseDTO result = userService.toggleUserStatus("1");
        assertNotNull(result);
        verify(userRepository).save(any());
    }

    @Test
    void getActiveUsers_ReturnsActiveUsers() {
        when(userRepository.findByActive(true)).thenReturn(Arrays.asList(user));
        List<UserResponseDTO> result = userService.getActiveUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void getInactiveUsers_ReturnsInactiveUsers() {
        user.setActive(false);
        when(userRepository.findByActive(false)).thenReturn(Arrays.asList(user));
        List<UserResponseDTO> result = userService.getInactiveUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isActive());
    }

    @Test
    void uploadProfilePicture_ValidFile_ReturnsImageUrl() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(any())).thenReturn("http://example.com/new-profile.jpg");
        when(userRepository.save(any())).thenReturn(user);

        String result = userService.uploadProfilePicture("1", file);
        assertNotNull(result);
        assertEquals("http://example.com/new-profile.jpg", result);
        verify(s3Service).uploadFile(file);
    }

    @Test
    void getProfilePicture_ExistingPicture_ReturnsUrl() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        String result = userService.getProfilePicture("1");
        assertNotNull(result);
        assertEquals(user.getProfilePictureUrl(), result);
    }

    @Test
    void getProfilePicture_NoPicture_ThrowsException() {
        user.setProfilePictureUrl(null);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> userService.getProfilePicture("1"));
    }

    @Test
    void updateProfilePicture_ValidFile_ReturnsNewUrl() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(any())).thenReturn("http://example.com/updated-profile.jpg");
        when(userRepository.save(any())).thenReturn(user);

        String result = userService.updateProfilePicture("1", file);
        assertNotNull(result);
        assertEquals("http://example.com/updated-profile.jpg", result);
        verify(s3Service).deleteFile(user.getProfilePictureUrl());
        verify(s3Service).uploadFile(file);
    }
} 