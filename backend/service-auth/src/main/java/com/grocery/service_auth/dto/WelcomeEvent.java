package com.grocery.service_auth.dto;

import java.io.Serializable;

/**
 * Event published to RabbitMQ after a new user is saved.
 * Consumed by service-email to send a Welcome email.
 */
public class WelcomeEvent implements Serializable {

    private String email;
    private String username;

    public WelcomeEvent() {}

    public WelcomeEvent(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
