package com.giahung19.ecommerce_api.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequestDTO {

    @NotBlank (message = "Username not blank")
    private String username;

    @NotBlank (message = "Password not blank")
    private String password;

    public RegisterRequestDTO() {}

    public RegisterRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
