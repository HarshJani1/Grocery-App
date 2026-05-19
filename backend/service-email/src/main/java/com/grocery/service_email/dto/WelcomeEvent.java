package com.grocery.service_email.dto;

import java.io.Serializable;

/**
 * Event published by service-auth after a new user signs up.
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
