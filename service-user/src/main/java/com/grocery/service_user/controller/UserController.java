package com.grocery.service_user.controller;

import com.grocery.service_user.entity.User;
import com.grocery.service_user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Map<String, Object> buildResponse(String status, String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", java.time.LocalDateTime.now());
        return body;
    }

    // ✅ CREATE USER
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody User user) {
        try {
            User saved = userService.addUser(user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(buildResponse("success", "User created successfully", saved));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to create user: " + e.getMessage(), null));
        }
    }

    // ✅ GET ALL USERS
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.getUsers();
            return ResponseEntity
                    .ok(buildResponse("success", "Users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to fetch users: " + e.getMessage(), null));
        }
    }

    // ✅ GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable long id) {
        try {
            User user = userService.getUser(id);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(buildResponse("error", "User not found", null));
            }
            return ResponseEntity
                    .ok(buildResponse("success", "User fetched successfully", user));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Error fetching user: " + e.getMessage(), null));
        }
    }

    // ✅ UPDATE USER
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user) {
        try {
            User updated = userService.updateUser(user);
            return ResponseEntity
                    .ok(buildResponse("success", "User updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(buildResponse("error", "Failed to update user: " + e.getMessage(), null));
        }
    }

    // ✅ DELETE USER
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity
                    .ok(buildResponse("success", "User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "User not found", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to delete user: " + e.getMessage(), null));
        }
    }
}
