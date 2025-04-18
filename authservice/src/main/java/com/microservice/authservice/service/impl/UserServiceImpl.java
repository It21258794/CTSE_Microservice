package com.microservice.authservice.service.impl;

import com.microservice.authservice.dto.UserDTO;
import com.microservice.authservice.dto.UserResponseDTO;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.service.UserService;
import com.microservice.authservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(String id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRole(userDTO.getRole());
        existingUser.setActive(userDTO.isActive());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToUserResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    @Override
    public UserResponseDTO toggleUserStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(!user.isActive());
        User updatedUser = userRepository.save(user);

        return mapToUserResponseDTO(updatedUser);
    }

    @Override
    public List<UserResponseDTO> getActiveUsers() {
        return userRepository.findByActive(true).stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> getInactiveUsers() {
        return userRepository.findByActive(false).stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String uploadProfilePicture(String userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = s3Service.uploadFile(file);
        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    @Override
    public String getProfilePicture(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
            throw new RuntimeException("Profile picture not found for user");
        }
        return user.getProfilePictureUrl();
    }

    @Override
    public String updateProfilePicture(String userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            deleteProfilePictureFromS3(user.getProfilePictureUrl());
        }

        String imageUrl = s3Service.uploadFile(file);
        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    @Override
    public void deleteProfilePicture(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            deleteProfilePictureFromS3(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
            userRepository.save(user);
        }
    }

    private void deleteProfilePictureFromS3(String imageUrl) {
        try {
            s3Service.deleteFile(imageUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete profile picture from S3", e);
        }
    }
} 