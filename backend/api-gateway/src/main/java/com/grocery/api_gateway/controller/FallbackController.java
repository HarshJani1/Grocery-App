package com.grocery.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private Mono<ResponseEntity<Map<String, Object>>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("data", null);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/auth-service")
    public Mono<ResponseEntity<Map<String, Object>>> authServiceFallback() {
        return createFallbackResponse("Authentication Service");
    }

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return createFallbackResponse("User Service");
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return createFallbackResponse("Product Service");
    }

    @GetMapping("/ml-service")
    public Mono<ResponseEntity<Map<String, Object>>> mlServiceFallback() {
        return createFallbackResponse("ML Service");
    }

    @GetMapping("/cart-service")
    public Mono<ResponseEntity<Map<String, Object>>> cartServiceFallback() {
        return createFallbackResponse("Cart Service");
    }

    @GetMapping("/email-service")
    public Mono<ResponseEntity<Map<String, Object>>> emailServiceFallback() {
        return createFallbackResponse("Email Service");
    }
}
