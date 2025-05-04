package com.microservice.authservice.security.filters;

import com.microservice.authservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip token validation for all auth endpoints and other public paths
        boolean shouldSkip = path.startsWith("/api/v1/auth/") || 
                           path.startsWith("/swagger-ui/") || 
                           path.startsWith("/v3/api-docs/") ||
                           path.startsWith("/public/");
        
        if (shouldSkip) {
            System.out.println("TokenAuthenticationFilter: Skipping filter for path: " + path);
            // Set anonymous authentication for skipped paths
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        System.out.println("TokenAuthenticationFilter: Processing request: " + requestURI);

        String authHeader = request.getHeader("Authorization");
        System.out.println("TokenAuthenticationFilter: Authorization header: " + (authHeader != null ? "present" : "missing"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            System.out.println("TokenAuthenticationFilter: Validating JWT token");
            Map<String, Object> claims = null;
            try {
                claims = tokenService.validateToken(jwt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (claims != null) {
                System.out.println("TokenAuthenticationFilter: Token validation successful");
                OAuth2User user = new DefaultOAuth2User(
                        null,
                        claims,
                        "sub"
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("TokenAuthenticationFilter: Authentication set in SecurityContext");
                filterChain.doFilter(request, response);
                return;
            } else {
                System.out.println("TokenAuthenticationFilter: Token validation failed");
            }
        } else {
            System.out.println("TokenAuthenticationFilter: No valid Authorization header found");
        }

        System.out.println("TokenAuthenticationFilter: Proceeding to OAuth2 filter");
        filterChain.doFilter(request, response);
    }
} 