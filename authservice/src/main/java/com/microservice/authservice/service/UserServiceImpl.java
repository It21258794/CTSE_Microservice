package com.microservice.authservice.service;

import com.microservice.authservice.dto.UserDTO;
import com.microservice.authservice.dto.UserResponseDTO;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
        existingUser.setStatus(userDTO.getStatus());

        // Only update password if it's provided in the DTO
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
                .status(user.getStatus())
                .build();
    }

    @Override
    public UserResponseDTO toggleUserStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Toggle between Active/Deactive
        String newStatus = user.getStatus().equalsIgnoreCase("Active")
                ? "Deactive"
                : "Active";

        user.setStatus(newStatus);
        User updatedUser = userRepository.save(user);

        return mapToUserResponseDTO(updatedUser);
    }

    @Override
    public List<UserResponseDTO> getActiveUsers() {
        return userRepository.findByStatus("Active").stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> getDeactiveUsers() {
        return userRepository.findByStatus("Deactive").stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }
}