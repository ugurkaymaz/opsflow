package com.opsflow.backend.dto;

import com.opsflow.backend.entity.UserRole;

public class AuthResponse {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String token;

    public AuthResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            String token) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}