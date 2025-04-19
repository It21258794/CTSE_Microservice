package com.microservice.authservice.security;

import com.microservice.authservice.model.Role;
import com.microservice.authservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "jwt.expiration=86400000"
})
class JwtServiceTest {
    private JwtService jwtService;
    private User user;
    private String testSecretKey;
    private static final long TEST_EXPIRATION = 86400000;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Generate a random 256-bit (32-byte) key
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        testSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);

        user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .role(Role.USER.name())
                .active(true)
                .build();
    }

    @Test
    void generateToken_ValidUserDetails_ReturnsToken() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateToken_WithExtraClaims_ReturnsToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtService.generateToken(extraClaims, user);
        assertNotNull(token);
        Claims claims = extractClaims(token);
        assertEquals("customValue", claims.get("customClaim"));
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(user);
        boolean isValid = jwtService.isTokenValid(token, user);
        assertTrue(isValid);
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        assertEquals(user.getEmail(), username);
    }

    private Claims extractClaims(String token) {
        Key key = Keys.hmacShaKeyFor(testSecretKey.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
} 