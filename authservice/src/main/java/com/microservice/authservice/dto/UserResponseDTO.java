package com.microservice.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean isActive;
    private String profilePictureUrl;  // Add this field

}