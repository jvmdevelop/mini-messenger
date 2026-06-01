package com.jvmdevelop.strife.controller;

import com.jvmdevelop.strife.dto.UserDto;
import com.jvmdevelop.strife.model.User;
import com.jvmdevelop.strife.reqandresp.AuthResponse;
import com.jvmdevelop.strife.service.UserService;
import com.jvmdevelop.strife.utils.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto user) {
        String hashPassword = passwordEncoder.encode(user.getPassword());
        userService.add(User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(hashPassword)
                .description(user.getDescription() != null ? user.getDescription() : "")
                .role(user.getRole() != null ? user.getRole() : "USER")
                .avatarUrl(user.getAvatarUrl())
                .build());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(AuthResponse.success(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserDto user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(AuthResponse.success(token));
    }
}
