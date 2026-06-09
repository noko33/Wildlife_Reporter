package com.wildlifedb.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.exception.ResourceNotFoundException;
import com.wildlifedb.repository.UserRepository;
import com.wildlifedb.security.JwtService;
import com.wildlifedb.security.SecurityErrorResponseWriter;
import com.wildlifedb.service.ObservationService;

@WebMvcTest(ObservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ObservationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationService observationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @Test
    void returnsPagedObservationResponse() throws Exception {
        ObservationResponse item = new ObservationResponse(
                42,
                "observer",
                "Panthera leo",
                "Lion",
                "Panthera",
                "Felidae",
                "Carnivora",
                "Mammalia",
                "Chordata",
                "Animalia",
                true,
                "Seen near the trail",
                4,
                OffsetDateTime.parse("2026-01-10T12:00:00Z"),
                -89.65f,
                39.78f,
                "Springfield",
                "IL",
                "Grassland");
        PageResponse<ObservationResponse> page =
                new PageResponse<>(1, 1, 0, 10, List.of(item));

        when(observationService.findObservations(
                eq(0),
                eq(10),
                eq("lion"),
                eq("spring"),
                any(),
                any(),
                eq("felidae")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/observations")
                        .param("page", "0")
                        .param("size", "10")
                        .param("speciesName", "lion")
                        .param("location", "spring")
                        .param("startDate", "2026-01-01T00:00:00Z")
                        .param("endDate", "2026-01-31T23:59:59Z")
                        .param("taxonomy", "felidae"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.items[0].id").value(42))
                .andExpect(jsonPath("$.data.items[0].speciesName").value("Panthera leo"));
    }

    @Test
    void supportsUnversionedPathAndCategoryAlias() throws Exception {
        PageResponse<ObservationResponse> emptyPage =
                new PageResponse<>(0, 0, 0, 20, List.of());

        when(observationService.findObservations(
                0,
                20,
                null,
                null,
                null,
                null,
                "mammalia"))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/observations").param("category", "mammalia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void returnsBadRequestForInvalidPage() throws Exception {
        when(observationService.findObservations(
                -1,
                20,
                null,
                null,
                null,
                null,
                null))
                .thenThrow(new IllegalArgumentException(
                        "page must be greater than or equal to 0"));

        mockMvc.perform(get("/observations").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message")
                        .value("page must be greater than or equal to 0"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void createsObservationForAuthenticatedUser() throws Exception {
        ObservationResponse response = observation();
        when(observationService.createObservation(eq("owner@example.com"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/observations")
                        .principal(new TestingAuthenticationToken(
                                "owner@example.com",
                                null))
                        .contentType("application/json")
                        .content("""
                                {
                                  "speciesName": "Panthera leo",
                                  "locationId": 7,
                                  "observedAt": "2026-05-12T08:30:00Z",
                                  "comment": "Adult animal near the river",
                                  "ageApproximation": 4,
                                  "longitude": -89.65,
                                  "latitude": 39.78
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Observation created successfully"))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.speciesName").value("Panthera leo"));

        verify(observationService).createObservation(eq("owner@example.com"), any());
    }

    @Test
    void rejectsCreateRequestWithMissingSpecies() throws Exception {
        mockMvc.perform(post("/observations")
                        .principal(new TestingAuthenticationToken(
                                "owner@example.com",
                                null))
                        .contentType("application/json")
                        .content("""
                                {
                                  "locationId": 7,
                                  "comment": "Missing species"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data.speciesName").value("speciesName is required"));

        verify(observationService, never()).createObservation(any(), any());
    }

    @Test
    void returnsNotFoundWhenCreateReferencesMissingSpecies() throws Exception {
        when(observationService.createObservation(eq("owner@example.com"), any()))
                .thenThrow(new ResourceNotFoundException(
                        "Species not found with name: Missing species"));

        mockMvc.perform(post("/observations")
                        .principal(new TestingAuthenticationToken(
                                "owner@example.com",
                                null))
                        .contentType("application/json")
                        .content("""
                                {
                                  "speciesName": "Missing species",
                                  "locationId": 7
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Species not found with name: Missing species"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private ObservationResponse observation() {
        return new ObservationResponse(
                42,
                "user-1",
                "Panthera leo",
                "Lion",
                "Panthera",
                "Felidae",
                "Carnivora",
                "Mammalia",
                "Chordata",
                "Animalia",
                false,
                "Adult animal near the river",
                4,
                OffsetDateTime.parse("2026-05-12T08:30:00Z"),
                -89.65f,
                39.78f,
                "Springfield",
                "IL",
                "Grassland");
    }
}
