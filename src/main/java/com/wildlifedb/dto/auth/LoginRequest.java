package com.wildlifedb.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Registered email", example = "user@example.com")
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @Schema(
                description = "Account password",
                example = "StrongPass123",
                format = "password")
        @NotBlank(message = "password is required")
        String password) {
}
