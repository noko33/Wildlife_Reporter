package com.wildlifedb.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(
                description = "Public user identifier",
                example = "wildlife_fan",
                minLength = 3,
                maxLength = 50)
        @NotBlank(message = "userId is required")
        @Size(min = 3, max = 50, message = "userId must be between 3 and 50 characters")
        String userId,

        @Schema(description = "Unique login email", example = "user@example.com")
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @Schema(
                description = "Password stored as a BCrypt hash",
                example = "StrongPass123",
                minLength = 8,
                maxLength = 72,
                format = "password")
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password) {
}
