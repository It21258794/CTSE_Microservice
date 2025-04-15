package com.microservice.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secure")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from protected endpoint!";
    }
}
