package com.ecommercegt.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 72) String password
    ) {}

    public record UserResponse(
            Long id,
            String name,
            String email,
            String role,
            Boolean active
    ) {}

    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token) {}
}
