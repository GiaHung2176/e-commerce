package com.giahung19.ecommerce_api.dto;

import com.giahung19.ecommerce_api.entity.Role;

public class AuthResponseDTO {

    private String token;
    private String username;
    private Role role;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String username, Role role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
