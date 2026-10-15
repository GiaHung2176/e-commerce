package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.AuthResponseDTO;
import com.giahung19.ecommerce_api.dto.LoginRequestDTO;
import com.giahung19.ecommerce_api.dto.RegisterRequestDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO registerRequest);

    AuthResponseDTO login(LoginRequestDTO loginRequest);
}
