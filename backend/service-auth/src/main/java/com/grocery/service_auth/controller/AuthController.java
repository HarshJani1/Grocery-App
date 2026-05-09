package com.grocery.service_auth.controller;

import com.grocery.service_auth.response.ApiResponse;
import com.grocery.service_auth.dto.AuthRequest;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.response.UserDTO;
import com.grocery.service_auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        log.info("POST /auth/register - Registering user with email: {}", user.getEmail());
        try {
            String result = service.saveUser(user);
            MDC.put("statusCode", String.valueOf(HttpStatus.CREATED.value()));
            log.info("User registered successfully | email={}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED, "User registered successfully", result));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to register user | email={} | error={}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", e.getMessage()));
        } finally {
            MDC.clear();
        }
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<UserDTO>> generateToken(@RequestBody AuthRequest authRequest) {
        log.info("POST /auth/token - Generating token for email: {}", authRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

            Optional<User> user = authService.findUserByEmail(authRequest.getEmail());

            if (authentication.isAuthenticated() && user.isPresent()) {

                String token = service.generateToken(authRequest.getEmail());
                User u = user.get();

                UserDTO dto = new UserDTO(
                        u.getUsername(),
                        u.getEmail(),
                        u.getPhoneNumber(),
                        u.getAddress(),
                        u.getRole(),
                        token
                );

                MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
                log.info("Token generated successfully | email={}", authRequest.getEmail());
                return ResponseEntity.ok(
                        new ApiResponse<>(
                                HttpStatus.OK,
                                "Token generated successfully",
                                dto
                        )
                );
            } else {
                MDC.put("statusCode", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
                log.warn("Authentication failed - invalid credentials | email={}", authRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password",
                                null
                        ));
            }
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
            log.error("Token generation failed | email={} | error={}", authRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid username or password",
                            null
                    ));
        } finally {
            MDC.clear();
        }
    }


    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam("token") String token) {
        log.info("GET /auth/validate - Validating token");
        try {
            service.validateToken(token);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Token validated successfully");
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Token is valid", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
            log.error("Token validation failed | error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Token is invalid", null));
        } finally {
            MDC.clear();
        }
    }
}