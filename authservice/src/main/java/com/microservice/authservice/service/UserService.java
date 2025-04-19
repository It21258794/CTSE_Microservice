package com.microservice.authservice.service;

import com.microservice.authservice.dto.UserDTO;
import com.microservice.authservice.dto.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(String id);
    UserResponseDTO updateUser(String id, UserDTO userDTO);
    void deleteUser(String id);
    UserResponseDTO toggleUserStatus(String id);
    List<UserResponseDTO> getActiveUsers();
    List<UserResponseDTO> getInactiveUsers();
    String uploadProfilePicture(String userId, MultipartFile file) throws IOException;
    String getProfilePicture(String userId);
    String updateProfilePicture(String userId, MultipartFile file) throws IOException;
    void deleteProfilePicture(String userId);
}