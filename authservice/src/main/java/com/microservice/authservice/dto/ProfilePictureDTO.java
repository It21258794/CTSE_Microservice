package com.microservice.authservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfilePictureDTO {
    private MultipartFile file;
}