package com.grocery.service_auth.response;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;

    public ApiResponse(HttpStatus status, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.message = message;
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}

