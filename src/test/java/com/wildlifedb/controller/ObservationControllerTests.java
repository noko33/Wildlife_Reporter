package com.wildlifedb.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.ObservationResponse;
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
}
