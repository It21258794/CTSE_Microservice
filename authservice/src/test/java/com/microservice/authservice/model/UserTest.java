package com.microservice.authservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .role("USER")
                .active(true)
                .profilePictureUrl("http://example.com/profile.jpg")
                .build();
    }

    @Test
    void getAuthorities_ReturnsCorrectRole() {
        Collection<?> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void getUsername_ReturnsEmail() {
        assertEquals("john.doe@example.com", user.getUsername());
    }

    @Test
    void getPassword_ReturnsPassword() {
        assertEquals("password123", user.getPassword());
    }

    @Test
    void isAccountNonExpired_ReturnsTrue() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ReturnsTrue() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ReturnsTrue() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_WhenActive_ReturnsTrue() {
        assertTrue(user.isEnabled());
    }

    @Test
    void isEnabled_WhenInactive_ReturnsFalse() {
        user.setActive(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void builder_CreatesValidUser() {
        User builtUser = User.builder()
                .id("2")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("password456")
                .role("ADMIN")
                .active(true)
                .profilePictureUrl("http://example.com/jane.jpg")
                .build();

        assertNotNull(builtUser);
        assertEquals("2", builtUser.getId());
        assertEquals("Jane", builtUser.getFirstName());
        assertEquals("Smith", builtUser.getLastName());
        assertEquals("jane.smith@example.com", builtUser.getEmail());
        assertEquals("password456", builtUser.getPassword());
        assertEquals("ADMIN", builtUser.getRole());
        assertTrue(builtUser.isActive());
        assertEquals("http://example.com/jane.jpg", builtUser.getProfilePictureUrl());
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        User user1 = User.builder()
                .id("1")
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id("1")
                .email("test@example.com")
                .build();

        User user3 = User.builder()
                .id("2")
                .email("different@example.com")
                .build();

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void toString_ContainsAllFields() {
        String toString = user.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("firstName=John"));
        assertTrue(toString.contains("lastName=Doe"));
        assertTrue(toString.contains("email=john.doe@example.com"));
        assertTrue(toString.contains("role=USER"));
        assertTrue(toString.contains("active=true"));
        assertTrue(toString.contains("profilePictureUrl=http://example.com/profile.jpg"));
    }
} 