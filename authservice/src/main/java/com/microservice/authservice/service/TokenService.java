package com.microservice.authservice.service;

import com.microservice.authservice.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TokenService {

    private final JwtService jwtService;

    @Autowired
    public TokenService(JwtService jwtService) {
        this.jwtService = jwtService;
        System.out.println("TokenService initialized with JwtService");
    }

    public String generateToken(OAuth2User oAuth2User) {
        System.out.println("Generating OAuth2 token for user: " + oAuth2User.getName());
        String token = jwtService.generateToken(oAuth2User);
        System.out.println("OAuth2 token generated successfully");
        return token;
    }

    public Map<String, Object> validateToken(String token) throws Exception {
        System.out.println("Validating token: " + token);
        try {
            Claims claims = jwtService.extractAllClaims(token);
            if (!jwtService.isTokenExpired(token)) {
                System.out.println("Token is valid and not expired. Source: " + claims.get("source"));
                return claims;
            } else {
                System.out.println("Token has expired");
            }
        } catch (Exception e) {
            throw new Exception("Token validation failed", e);
        }
        return null;
    }
}
