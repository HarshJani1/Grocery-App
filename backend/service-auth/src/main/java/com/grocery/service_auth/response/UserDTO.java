package com.grocery.service_auth.response;

import com.grocery.service_auth.entity.Role;
import jakarta.persistence.*;

public class UserDTO {
    private String username;

    private String email;

    private String phoneNumber;

    private String address;

    private Role role;

    private String token;

    public UserDTO( String username, String email, String phoneNumber, String address, Role role, String token) {

        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.token = token;
    }


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
