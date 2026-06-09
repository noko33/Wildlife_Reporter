package com.wildlifedb.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildlifedb.dto.auth.AuthResponse;
import com.wildlifedb.dto.auth.LoginRequest;
import com.wildlifedb.dto.auth.RegisterRequest;
import com.wildlifedb.entity.User;
import com.wildlifedb.exception.DuplicateResourceException;
import com.wildlifedb.repository.UserRepository;
import com.wildlifedb.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedUserId = request.userId().trim();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByUserId(normalizedUserId)) {
            throw new DuplicateResourceException("userId is already registered");
        }

        User user = new User();
        user.setUserId(normalizedUserId);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setVerifier(false);
        User savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = authenticateUser(request.email(), request.password());
        return createAuthResponse(user);
    }

    @Transactional
    public User authenticateUser(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid email or password"));

        if (isBcryptHash(user.getPassword())) {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new BadCredentialsException("Invalid email or password");
            }
        } else if (user.getPassword() != null && user.getPassword().equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
        return user;
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getUserId(),
                user.getEmail());
    }

    private boolean isBcryptHash(String password) {
        return password != null
                && (password.startsWith("$2a$")
                        || password.startsWith("$2b$")
                        || password.startsWith("$2y$"));
    }
}
