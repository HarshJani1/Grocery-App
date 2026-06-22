package com.grocery.service_auth.config;

import com.grocery.service_auth.entity.Role;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.repository.UserCredentialRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserCredentialRepository repository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername - success returns UserDetails")
    void loadUserByUsername_success_returnsUserDetails() {
        User user = new User(1L, "John", "john@grocery.com", "pass", "123", "Addr", Role.CUSTOMER);
        when(repository.findByEmail("john@grocery.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("john@grocery.com");

        assertNotNull(userDetails);
        assertEquals("john@grocery.com", userDetails.getUsername());
        assertEquals("pass", userDetails.getPassword());
    }

    @Test
    @DisplayName("loadUserByUsername - not found throws UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsException() {
        when(repository.findByEmail("missing@grocery.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("missing@grocery.com");
        });
    }
}
