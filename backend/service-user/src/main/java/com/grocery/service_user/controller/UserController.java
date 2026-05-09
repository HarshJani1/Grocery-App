package com.grocery.service_user.controller;

import com.grocery.service_user.DTO.UserDTO;
import com.grocery.service_user.entity.User;
import com.grocery.service_user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

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

    // GET ALL USERS
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        log.info("GET /users/get - Fetching all users");
        try {
            List<User> users = userService.getUsers();
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Users fetched successfully | count={}", users.size());
            return ResponseEntity
                    .ok(buildResponse("success", "Users fetched successfully", users));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to fetch users | error={}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to fetch users: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable long id) {
        log.info("GET /users/{} - Fetching user by ID", id);
        try {
            User user = userService.getUser(id);
            if (user == null) {
                MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
                log.warn("User not found | id={}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(buildResponse("error", "User not found", null));
            }
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("User fetched successfully | id={} | email={}", id, user.getEmail());
            return ResponseEntity
                    .ok(buildResponse("success", "User fetched successfully", user));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Error fetching user | id={} | error={}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Error fetching user: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // UPDATE USER
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody UserDTO user,@RequestHeader String email) {
        log.info("PUT /users/update - Updating user | email={}", email);
        try {
            User updated = userService.updateUser(user,email);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("User updated successfully | email={}", email);
            return ResponseEntity
                    .ok(buildResponse("success", "User updated successfully", updated));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            log.error("Failed to update user | email={} | error={}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(buildResponse("error", "Failed to update user: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // DELETE USER
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestHeader String email) {
        log.info("DELETE /users/delete - Deleting user | email={}", email);
        try {
            userService.deleteUser(email);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("User deleted successfully | email={}", email);
            return ResponseEntity
                    .ok(buildResponse("success", "User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("User not found for deletion | email={} | error={}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "User not found", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to delete user | email={} | error={}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to delete user: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }


}
