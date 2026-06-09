package com.wildlifedb.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.User;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityIntegrationTests {

    private static final String SPECIES_NAME = "Testus securitus";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUpSpecies() {
        if (speciesRepository.findBySpeciesId(SPECIES_NAME).isEmpty()) {
            speciesRepository.save(new Species(
                    SPECIES_NAME,
                    "Security Test Species",
                    false,
                    null));
        }
    }

    @Test
    void registerHashesPasswordAndReturnsToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "secure-user",
                                  "email": "secure-user@example.com",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.userId").value("secure-user"));

        User savedUser = userRepository.findByEmailIgnoreCase("secure-user@example.com")
                .orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("strong-password");
        assertThat(passwordEncoder.matches("strong-password", savedUser.getPassword())).isTrue();
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        User user = new User();
        user.setUserId("login-user");
        user.setEmail("login-user@example.com");
        user.setPassword(passwordEncoder.encode("login-password"));
        user.setVerifier(false);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login-user@example.com",
                                  "password": "login-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }

    @Test
    void observationQueryRemainsPublic() throws Exception {
        mockMvc.perform(get("/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void protectedObservationWriteRequiresToken() throws Exception {
        mockMvc.perform(post("/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "speciesName": "Testus securitus"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication token is required"));

        mockMvc.perform(put("/observations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(delete("/observations/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void invalidAndExpiredTokensReturnUnifiedErrors() throws Exception {
        mockMvc.perform(post("/observations")
                        .header("Authorization", "Bearer not-a-valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "speciesName": "Testus securitus"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Invalid JWT token"));

        mockMvc.perform(post("/observations")
                        .header("Authorization", "Bearer " + createExpiredToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "speciesName": "Testus securitus"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("JWT token has expired"));
    }

    @Test
    void validTokenCanCreateObservation() throws Exception {
        String token = registerAndReadToken(
                "observation-owner",
                "observation-owner@example.com",
                "owner-password");

        mockMvc.perform(post("/observations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "speciesName": "Testus securitus",
                                  "comment": "Created with JWT",
                                  "latitude": 40.0,
                                  "longitude": -88.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.observerUserId")
                        .value("observation-owner"))
                .andExpect(jsonPath("$.data.speciesName")
                        .value(SPECIES_NAME))
                .andExpect(jsonPath("$.data.comment")
                        .value("Created with JWT"));
    }

    private String registerAndReadToken(
            String userId,
            String email,
            String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(userId, email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    private String createExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("expired@example.com")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(key)
                .compact();
    }
}
