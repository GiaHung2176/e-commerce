package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.AuthResponseDTO;
import com.giahung19.ecommerce_api.dto.LoginRequestDTO;
import com.giahung19.ecommerce_api.dto.RegisterRequestDTO;
import com.giahung19.ecommerce_api.entity.Role;
import com.giahung19.ecommerce_api.entity.User;
import com.giahung19.ecommerce_api.exception.DataConflictException;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.repository.UserRepository;
import com.giahung19.ecommerce_api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
}

    @Override
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DataConflictException("Username already exists");
        }

        User newUser = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                Role.CUSTOMER);
        User savedUser = userRepository.save(newUser);

        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole());
            return new AuthResponseDTO(token, savedUser.getUsername(), savedUser.getRole());
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()));

        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
            return new AuthResponseDTO(token, user.getUsername(), user.getRole());
    }

}
