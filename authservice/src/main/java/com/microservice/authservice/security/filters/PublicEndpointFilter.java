package com.microservice.authservice.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class PublicEndpointFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        System.out.println("PublicEndpointFilter: Processing request: " + requestURI);

        // List of public endpoints
        if (requestURI.startsWith("/api/v1/auth/register") ||
            requestURI.startsWith("/api/v1/auth/login") ||
            requestURI.startsWith("/api/v1/auth/forgot-password") ||
            requestURI.startsWith("/api/v1/auth/reset-password") ||
            requestURI.startsWith("/swagger-ui/") ||
            requestURI.startsWith("/v3/api-docs/") ||
            requestURI.startsWith("/public/")) {
            
            System.out.println("PublicEndpointFilter: Allowing access to public endpoint: " + requestURI);
            
            // Set anonymous authentication for public endpoints
            AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
                "public",
                "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
            
            filterChain.doFilter(request, response);
            return;
        }

        // For non-public endpoints, continue with the filter chain
        filterChain.doFilter(request, response);
    }
} 