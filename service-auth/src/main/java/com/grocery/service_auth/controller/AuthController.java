package com.grocery.service_auth.controller;

import com.grocery.service_auth.response.ApiResponse;
import com.grocery.service_auth.dto.AuthRequest;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody User user) {
        String result = service.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, "User registered successfully", result));
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<String>> generateToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = service.generateToken(authRequest.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Token generated successfully", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Invalid username or password", null));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Token is valid", null));
    }
}
