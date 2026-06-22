package com.grocery.service_user.controller;

import com.grocery.service_user.DTO.UserDTO;
import com.grocery.service_user.entity.Role;
import com.grocery.service_user.entity.User;
import com.grocery.service_user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User(1L, "john", "john@grocery.com", "password", "123456", "street 1", Role.CUSTOMER);
        userDTO = new UserDTO();
        userDTO.setUsername("johnUpdated");
        userDTO.setEmail("john.updated@grocery.com");
        userDTO.setPhoneNumber("654321");
        userDTO.setAddress("street 2");
    }

    // ── getAllUsers ──────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers - success returns 200 with list of users")
    void getAllUsers_success_returns200() {
        when(userService.getUsers()).thenReturn(Arrays.asList(user));

        ResponseEntity<Map<String, Object>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Users fetched successfully", response.getBody().get("message"));
        assertEquals(Arrays.asList(user), response.getBody().get("data"));
    }

    @Test
    @DisplayName("getAllUsers - exception returns 500")
    void getAllUsers_exception_returns500() {
        when(userService.getUsers()).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Map<String, Object>> response = userController.getAllUsers();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Failed to fetch users"));
    }

    // ── getUserById ──────────────────────────────────────────────

    @Test
    @DisplayName("getUserById - user exists returns 200")
    void getUserById_exists_returns200() {
        when(userService.getUser(1L)).thenReturn(user);

        ResponseEntity<Map<String, Object>> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(user, response.getBody().get("data"));
    }

    @Test
    @DisplayName("getUserById - user not found returns 404")
    void getUserById_notFound_returns404() {
        when(userService.getUser(99L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = userController.getUserById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("getUserById - exception returns 500")
    void getUserById_exception_returns500() {
        when(userService.getUser(1L)).thenThrow(new RuntimeException("Connection issue"));

        ResponseEntity<Map<String, Object>> response = userController.getUserById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }

    // ── updateUser ───────────────────────────────────────────────

    @Test
    @DisplayName("updateUser - success returns 200")
    void updateUser_success_returns200() {
        when(userService.updateUser(any(UserDTO.class), eq("john@grocery.com"))).thenReturn(user);

        ResponseEntity<Map<String, Object>> response = userController.updateUser(userDTO, "john@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(user, response.getBody().get("data"));
    }

    @Test
    @DisplayName("updateUser - exception returns 400")
    void updateUser_exception_returns400() {
        when(userService.updateUser(any(UserDTO.class), eq("john@grocery.com")))
                .thenThrow(new RuntimeException("Validation failed"));

        ResponseEntity<Map<String, Object>> response = userController.updateUser(userDTO, "john@grocery.com");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }

    // ── deleteUser ───────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser - success returns 200")
    void deleteUser_success_returns200() {
        doNothing().when(userService).deleteUser("john@grocery.com");

        ResponseEntity<Map<String, Object>> response = userController.deleteUser("john@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("User deleted successfully", response.getBody().get("message"));
    }

    @Test
    @DisplayName("deleteUser - not found returns 404")
    void deleteUser_notFound_returns404() {
        doThrow(new IllegalArgumentException("Not found")).when(userService).deleteUser("missing@grocery.com");

        ResponseEntity<Map<String, Object>> response = userController.deleteUser("missing@grocery.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("deleteUser - other exception returns 500")
    void deleteUser_exception_returns500() {
        doThrow(new RuntimeException("Database error")).when(userService).deleteUser("john@grocery.com");

        ResponseEntity<Map<String, Object>> response = userController.deleteUser("john@grocery.com");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }
}
