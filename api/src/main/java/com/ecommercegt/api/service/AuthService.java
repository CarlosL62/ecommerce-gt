package com.ecommercegt.api.service;

import com.ecommercegt.api.dto.AuthDtos;
import com.ecommercegt.api.model.Role;
import com.ecommercegt.api.model.User;
import com.ecommercegt.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.UserResponse register(AuthDtos.RegisterRequest req) {
        // Validates if email is already registered
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        // Encode password
        String hashed = encoder.encode(req.password());

        // Creates user entity
        User u = new User(req.name(), req.email(), hashed, Role.COMMON);

        User saved = users.save(u);

        return new AuthDtos.UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.isActive()
        );
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        var user = users.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario suspendido");
        }
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        String token = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token);
    }

}
