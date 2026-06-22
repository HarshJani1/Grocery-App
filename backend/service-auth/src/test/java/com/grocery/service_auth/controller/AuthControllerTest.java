package com.grocery.service_auth.controller;

import com.grocery.service_auth.entity.Role;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.response.ApiResponse;
import com.grocery.service_auth.response.UserDTO;
import com.grocery.service_auth.dto.AuthRequest;
import com.grocery.service_auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService service;

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "TestUser", "test@grocery.com",
                "password", "1234567890", "123 Street", Role.CUSTOMER);
    }

    // ── registerUser ─────────────────────────────────────────────

    @Test
    @DisplayName("registerUser - success returns 201 CREATED")
    void registerUser_success_returns201() {
        when(service.saveUser(any(User.class))).thenReturn("user added to the system");

        ResponseEntity<?> response = authController.registerUser(testUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("registerUser - exception returns 500 INTERNAL_SERVER_ERROR")
    void registerUser_exception_returns500() {
        when(service.saveUser(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = authController.registerUser(testUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ── generateToken ────────────────────────────────────────────

    @Test
    @DisplayName("generateToken - valid credentials returns 200 with UserDTO")
    void generateToken_validCredentials_returns200() {
        AuthRequest request = new AuthRequest("test@grocery.com", "password");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(authService.findUserByEmail("test@grocery.com")).thenReturn(testUser);
        when(service.generateToken("test@grocery.com")).thenReturn("jwt-token-123");

        ResponseEntity<ApiResponse<UserDTO>> response = authController.generateToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Token generated successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("generateToken - bad credentials returns 401 UNAUTHORIZED")
    void generateToken_badCredentials_returns401() {
        AuthRequest request = new AuthRequest("test@grocery.com", "wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<ApiResponse<UserDTO>> response = authController.generateToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("generateToken - user not found returns 401 UNAUTHORIZED")
    void generateToken_userNotFound_returns401() {
        AuthRequest request = new AuthRequest("ghost@grocery.com", "password");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any()))
                .thenReturn(mockAuth);
        when(authService.findUserByEmail("ghost@grocery.com")).thenReturn(null);

        ResponseEntity<ApiResponse<UserDTO>> response = authController.generateToken(request);

        // user == null → else branch returns UNAUTHORIZED
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ── validateToken ────────────────────────────────────────────

    @Test
    @DisplayName("validateToken - valid token returns 200")
    void validateToken_valid_returns200() {
        when(service.validateToken("valid-token")).thenReturn(true);

        ResponseEntity<ApiResponse<String>> response =
                authController.validateToken("valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token is valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("validateToken - invalid token returns 401 UNAUTHORIZED")
    void validateToken_invalid_returns401() {
        when(service.validateToken("bad-token")).thenThrow(new RuntimeException("Expired"));

        ResponseEntity<ApiResponse<String>> response =
                authController.validateToken("bad-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token is invalid", response.getBody().getMessage());
    }
}
