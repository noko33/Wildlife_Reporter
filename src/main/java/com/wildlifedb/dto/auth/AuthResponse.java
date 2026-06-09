package com.wildlifedb.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Signed JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(description = "Authorization scheme", example = "Bearer")
        String tokenType,
        @Schema(description = "Token lifetime in seconds", example = "3600")
        long expiresIn,
        @Schema(description = "Public user identifier", example = "wildlife_fan")
        String userId,
        @Schema(description = "Account email", example = "user@example.com")
        String email) {
}
