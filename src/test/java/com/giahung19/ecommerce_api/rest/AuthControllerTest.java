package com.giahung19.ecommerce_api.rest;

import com.giahung19.ecommerce_api.dto.AuthResponseDTO;
import com.giahung19.ecommerce_api.entity.Role;
import com.giahung19.ecommerce_api.exception.DataConflictException;
import com.giahung19.ecommerce_api.security.CustomUserDetailsService;
import com.giahung19.ecommerce_api.security.JwtAccessDeniedHandler;
import com.giahung19.ecommerce_api.security.JwtAuthenticationEntryPoint;
import com.giahung19.ecommerce_api.security.JwtService;
import com.giahung19.ecommerce_api.security.SecurityConfig;
import com.giahung19.ecommerce_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // 🧪 Test case 1: register, body hợp lệ, không token -> 201 (đường dẫn permitAll)
    @Test
    void register_WhenBodyValid_ShouldReturn201WithToken() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponseDTO("jwt-token", "alice", Role.CUSTOMER));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // 🧪 Test case 2: register, username rỗng -> 400, lỗi theo field username
    @Test
    void register_WhenUsernameBlank_ShouldReturn400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("username"));
    }

    // 🧪 Test case 3: register, username trùng -> 409
    @Test
    void register_WhenUsernameAlreadyExists_ShouldReturn409() throws Exception {
        when (authService.register(any())).thenThrow(new DataConflictException("Username already exists"));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict());

    }

    // 🧪 Test case 4: login, body hợp lệ -> 200, có token
    @Test
    void login_WhenBodyValid_ShouldReturn200WithToken() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponseDTO("jwt-token", "alice", Role.CUSTOMER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // 🧪 Test case 5: login, password rỗng -> 400, lỗi theo field password
    @Test
    void login_WhenPasswordBlank_ShouldReturn400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    // 🧪 Test case 6: login, sai thông tin -> 401
    @Test
    void login_WhenCredentialsInvalid_ShouldReturn401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credentials is invalid"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

    }

    // 🧪 Test case 7: login, body không phải JSON hợp lệ -> 400
    @Test
    void login_WhenBodyMalformed_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());
    }
}
