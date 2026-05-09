package com.grocery.service_auth.service;

import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.repository.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private UserCredentialRepository repository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    public AuthService(UserCredentialRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public String saveUser(User credential) {
        log.info("Saving user | email={}", credential.getEmail());
        try {
            credential.setPassword(passwordEncoder.encode(credential.getPassword()));
            repository.save(credential);
            log.info("User saved successfully | email={}", credential.getEmail());
            return "user added to the system";
        } catch (Exception e) {
            log.error("Failed to save user | email={} | error={}", credential.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public Optional<User> findUserByEmail(String email) {
        log.debug("Finding user by email | email={}", email);
        Optional<User> user = repository.findByEmail(email);
        if (user.isPresent()) {
            log.debug("User found | email={}", email);
        } else {
            log.warn("User not found | email={}", email);
        }
        return user;
    }

    public String generateToken(String email) {
        log.info("Generating JWT token | email={}", email);
        try {
            String token = jwtService.generateToken(email);
            log.info("JWT token generated successfully | email={}", email);
            return token;
        } catch (Exception e) {
            log.error("JWT token generation failed | email={} | error={}", email, e.getMessage(), e);
            throw e;
        }
    }

    public void validateToken(String token) {
        log.debug("Validating JWT token");
        try {
            jwtService.validateToken(token);
            log.debug("JWT token is valid");
        } catch (Exception e) {
            log.error("JWT token validation failed | error={}", e.getMessage(), e);
            throw e;
        }
    }


}