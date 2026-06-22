package com.grocery.service_user.service;

import com.grocery.service_user.DTO.UserDTO;
import com.grocery.service_user.entity.Role;
import com.grocery.service_user.entity.User;
import com.grocery.service_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

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

    @Test
    @DisplayName("getUsers - returns all users from repository")
    void getUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> result = userService.getUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getUser - existing ID returns user")
    void getUser_existingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals("john@grocery.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUser - non-existing ID returns null")
    void getUser_nonExistingId_returnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        User result = userService.getUser(99L);

        assertNull(result);
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("updateUser - user exists updates and saves user")
    void updateUser_exists_updatesAndSaves() {
        when(userRepository.findByEmail("john@grocery.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userDTO, "john@grocery.com");

        assertNotNull(result);
        assertEquals("johnUpdated", result.getUsername());
        assertEquals("john.updated@grocery.com", result.getEmail());
        assertEquals("654321", result.getPhoneNumber());
        assertEquals("street 2", result.getAddress());
        verify(userRepository, times(1)).findByEmail("john@grocery.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - user does not exist returns null")
    void updateUser_notExists_returnsNull() {
        when(userRepository.findByEmail("missing@grocery.com")).thenReturn(null);

        User result = userService.updateUser(userDTO, "missing@grocery.com");

        assertNull(result);
        verify(userRepository, times(1)).findByEmail("missing@grocery.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - repository throws exception propagates exception")
    void updateUser_throwsException_propagates() {
        when(userRepository.findByEmail("john@grocery.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userDTO, "john@grocery.com");
        });
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("deleteUser - calls deleteByEmail on repository")
    void deleteUser_callsDeleteByEmail() {
        doNothing().when(userRepository).deleteByEmail("john@grocery.com");

        assertDoesNotThrow(() -> userService.deleteUser("john@grocery.com"));
        verify(userRepository, times(1)).deleteByEmail("john@grocery.com");
    }

    @Test
    @DisplayName("deleteUser - repository throws exception propagates")
    void deleteUser_throwsException_propagates() {
        doThrow(new RuntimeException("DB error")).when(userRepository).deleteByEmail("john@grocery.com");

        assertThrows(RuntimeException.class, () -> userService.deleteUser("john@grocery.com"));
        verify(userRepository, times(1)).deleteByEmail("john@grocery.com");
    }
}
