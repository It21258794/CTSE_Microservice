package com.microservice.authservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/") // Base URL mapping
public class MainPage {

    @GetMapping
    public String home() {
        return """
        """;
    }
}
