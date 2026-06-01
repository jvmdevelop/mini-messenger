package com.jvmdevelop.strife.controllers;

import com.jvmdevelop.strife.controller.AuthController;
import com.jvmdevelop.strife.dto.UserDto;
import com.jvmdevelop.strife.model.User;
import com.jvmdevelop.strife.reqandresp.AuthResponse;
import com.jvmdevelop.strife.service.UserService;
import com.jvmdevelop.strife.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnJwtToken_whenUserIsRegisteredSuccessfully() {
        UserDto userDto = new UserDto("testuser", "test@example.com", "password123", "desc", "USER", null);

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userService.add(any(User.class))).thenReturn(User.builder().username("testuser").build());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwtToken");

        ResponseEntity<AuthResponse> response = authController.register(userDto);

        assertNotNull(response.getBody());
        assertEquals("jwtToken", response.getBody().getToken());
        verify(userService, times(1)).add(any(User.class));
    }

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreValid() {
        UserDto userDto = new UserDto("testuser", "test@example.com", "password123", null, null, null);

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwtToken");

        ResponseEntity<AuthResponse> response = authController.login(userDto);

        assertNotNull(response.getBody());
        assertEquals("jwtToken", response.getBody().getToken());
    }

    @Test
    void register_shouldThrowException_whenUserServiceFails() {
        UserDto userDto = new UserDto("testuser", "test@example.com", "password123", "desc", "USER", null);

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        doThrow(new RuntimeException("User service failed")).when(userService).add(any(User.class));

        assertThrows(RuntimeException.class, () -> authController.register(userDto));
    }

    @Test
    void login_shouldThrowException_whenAuthenticationFails() {
        UserDto userDto = new UserDto("testuser", "test@example.com", "password123", null, null, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        assertThrows(RuntimeException.class, () -> authController.login(userDto));
    }
}
