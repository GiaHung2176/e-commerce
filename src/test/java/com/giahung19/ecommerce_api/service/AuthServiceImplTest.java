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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    // 🧪 Test case 1: username đã tồn tại -> 409, không lưu user
    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowDataConflictException() {
        when(userRepository.existsByUsername("Camia")).thenReturn(true);
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("Camia", "Not-a-real-password");

        assertThrows(DataConflictException.class,() ->authService.register(registerRequestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    // 🧪 Test case 2: hợp lệ -> mật khẩu được mã hóa trước khi lưu, role CUSTOMER, trả token
    @Test
    void register_WhenRequestIsValid_ShouldSaveEncodedPasswordWithCustomerRoleAndReturnToken() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("alice", "secret123");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("alice", Role.CUSTOMER)).thenReturn("jwt-token");

        AuthResponseDTO result = authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-secret", userCaptor.getValue().getPassword());
        assertEquals(Role.CUSTOMER, userCaptor.getValue().getRole());
        assertEquals("jwt-token", result.getToken());
        assertEquals("alice", result.getUsername());
    }

    // 🧪 Test case 3: thông tin đúng -> trả token và username
    @Test
    void login_WhenCredentialsAreValid_ShouldReturnTokenAndUsername() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("alice", "secret123");
        User user = new User("alice", "encoded-secret", Role.CUSTOMER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("alice", Role.CUSTOMER)).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(loginRequest);

        assertEquals("jwt-token", result.getToken());
        assertEquals("alice", result.getUsername());
        assertEquals(Role.CUSTOMER, result.getRole());
    }

    // 🧪 Test case 4: sai mật khẩu -> exception lan ra, không cấp token
    @Test
    void login_WhenPasswordIsWrong_ShouldThrowAndNotIssueToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad Credentials"));
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("Camia", "Not-a-real-password");
        assertThrows(BadCredentialsException.class,() ->authService.login(loginRequestDTO));

        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(anyString(), any());
    }

    // 🧪 Test case 5: xác thực qua nhưng không tìm thấy user trong DB -> 404
    @Test
    void login_WhenAuthenticatedUserIsMissingInDatabase_ShouldThrowResourceNotFoundException() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("ghost", "secret123");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));

        verify(jwtService, never()).generateToken(anyString(), any());
    }
}
