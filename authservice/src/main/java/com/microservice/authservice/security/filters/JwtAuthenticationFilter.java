package com.microservice.authservice.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.authservice.dto.ErrorResponse;
import com.microservice.authservice.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        String requestURI = request.getRequestURI();
        System.out.println("Processing request: " + requestURI);
        System.out.println("Request method: " + request.getMethod());
        System.out.println("Request headers: " + Collections.list(request.getHeaderNames()));
        
        // Skip JWT check for public endpoints
        if (requestURI.equals("/api/v1/auth/register") || 
            requestURI.equals("/api/v1/auth/login") ||
            requestURI.equals("/api/v1/auth/reset-password-request") ||
            requestURI.equals("/secure/hello")) {
            System.out.println("Skipping JWT check for public endpoint: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No JWT token found in request for path: " + requestURI);
            sendErrorResponse(response, "No JWT token found in request", HttpStatus.FORBIDDEN);
            return;
        }
        
        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("Extracted user email from JWT: " + userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("Loading user details for: " + userEmail);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("JWT token is valid for user: " + userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("JWT token is invalid for user: " + userEmail);
                    sendErrorResponse(response, "Invalid JWT token", HttpStatus.FORBIDDEN);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("Error processing JWT token: " + e.getMessage());
            sendErrorResponse(response, "Error processing JWT token: " + e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .error(status.getReasonPhrase())
                .status(status.value())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 