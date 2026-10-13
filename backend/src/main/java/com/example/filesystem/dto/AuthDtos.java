package com.example.filesystem.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() { }
    public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password) { }
    public record LoginRequest(@NotBlank String email, @NotBlank String password) { }
}
