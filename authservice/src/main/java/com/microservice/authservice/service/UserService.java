package com.microservice.authservice.service;

import com.microservice.authservice.dto.UserDTO;
import com.microservice.authservice.dto.UserResponseDTO;
import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(String id);
    UserResponseDTO updateUser(String id, UserDTO userDTO);
    void deleteUser(String id);
    UserResponseDTO toggleUserStatus(String id);
    List<UserResponseDTO> getActiveUsers();
    List<UserResponseDTO> getDeactiveUsers();
}