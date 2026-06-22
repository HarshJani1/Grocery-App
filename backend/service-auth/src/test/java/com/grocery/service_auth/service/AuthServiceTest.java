package com.grocery.service_auth.service;

import com.grocery.service_auth.entity.Role;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.repository.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "TestUser", "test@grocery.com",
                "plainPassword", "1234567890", "123 Street", Role.CUSTOMER);
    }

    // ── saveUser ─────────────────────────────────────────────────

    @Test
    @DisplayName("saveUser - should encode password before saving")
    void saveUser_encodesPassword() {
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        authService.saveUser(testUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("saveUser - should return success message")
    void saveUser_returnsSuccessMessage() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any(User.class))).thenReturn(testUser);

        String result = authService.saveUser(testUser);

        assertEquals("user added to the system", result);
    }

    @Test
    @DisplayName("saveUser - should publish WelcomeEvent to RabbitMQ")
    void saveUser_publishesWelcomeEvent() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any(User.class))).thenReturn(testUser);

        authService.saveUser(testUser);

        verify(rabbitTemplate).convertAndSend(
                eq("grocery.email.exchange"),
                eq("email.welcome"),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("saveUser - should rethrow exception when repository fails")
    void saveUser_repositoryFailure_throwsException() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> authService.saveUser(testUser));
    }

    // ── findUserByEmail ──────────────────────────────────────────

    @Test
    @DisplayName("findUserByEmail - should return user when found")
    void findUserByEmail_found_returnsUser() {
        when(repository.findByEmail("test@grocery.com")).thenReturn(Optional.of(testUser));

        User result = authService.findUserByEmail("test@grocery.com");

        assertNotNull(result);
        assertEquals("test@grocery.com", result.getEmail());
        assertEquals("TestUser", result.getUsername());
    }

    @Test
    @DisplayName("findUserByEmail - should return null when user not found")
    void findUserByEmail_notFound_returnsNull() {
        when(repository.findByEmail("ghost@grocery.com")).thenReturn(Optional.empty());

        User result = authService.findUserByEmail("ghost@grocery.com");

        assertNull(result);
    }

    // ── generateToken ────────────────────────────────────────────

    @Test
    @DisplayName("generateToken - should delegate to JwtService and return token")
    void generateToken_delegatesToJwtService() {
        when(jwtService.generateToken("test@grocery.com")).thenReturn("jwt-token-123");

        String token = authService.generateToken("test@grocery.com");

        assertEquals("jwt-token-123", token);
        verify(jwtService).generateToken("test@grocery.com");
    }

    @Test
    @DisplayName("generateToken - should rethrow exception from JwtService")
    void generateToken_jwtServiceFailure_throwsException() {
        when(jwtService.generateToken(anyString()))
                .thenThrow(new RuntimeException("JWT generation failed"));

        assertThrows(RuntimeException.class,
                () -> authService.generateToken("test@grocery.com"));
    }

    // ── validateToken ────────────────────────────────────────────

    @Test
    @DisplayName("validateToken - should return true for a valid token")
    void validateToken_validToken_returnsTrue() {
        doNothing().when(jwtService).validateToken("valid-token");

        boolean result = authService.validateToken("valid-token");

        assertTrue(result);
    }

    @Test
    @DisplayName("validateToken - should rethrow exception for invalid token")
    void validateToken_invalidToken_throwsException() {
        doThrow(new RuntimeException("Invalid token"))
                .when(jwtService).validateToken("bad-token");

        assertThrows(RuntimeException.class,
                () -> authService.validateToken("bad-token"));
    }
}
