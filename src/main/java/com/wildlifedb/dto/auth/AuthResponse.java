package com.wildlifedb.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        String userId,
        String email) {
}
