package com.ecommercegt.api.controller;

import com.ecommercegt.api.dto.AuthDtos;
import com.ecommercegt.api.repository.UserRepository;
import com.ecommercegt.api.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final UserRepository users;

    public AuthController(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.UserResponse register(@RequestBody @Valid AuthDtos.RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@RequestBody AuthDtos.LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(Authentication authentication) {
        String email = authentication.getName();
        var u = users.findByEmail(email).orElseThrow();
        return new AuthDtos.UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name(),
                u.isActive()
        );
    }
}
