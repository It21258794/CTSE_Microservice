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
public class AuthEndpointFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        System.out.println("AuthEndpointFilter: Processing request: " + requestURI);

        if (requestURI.startsWith("/api/v1/auth/")) {
            System.out.println("AuthEndpointFilter: Setting anonymous authentication for auth endpoint");
            
            // Set anonymous authentication
            AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
                "auth",
                "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            return;
        }

        // For non-auth endpoints, continue with the filter chain
        filterChain.doFilter(request, response);
    }
} 