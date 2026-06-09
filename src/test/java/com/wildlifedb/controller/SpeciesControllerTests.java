package com.wildlifedb.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Species;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.UserRepository;
import com.wildlifedb.security.JwtService;
import com.wildlifedb.security.SecurityErrorResponseWriter;
import com.wildlifedb.service.BackendService;

@WebMvcTest(RestBackendController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpeciesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackendService backendService;

    @MockitoBean
    private SpeciesRepository speciesRepository;

    @MockitoBean
    private LocationRepository locationRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @Test
    void returnsRandomSpeciesId() throws Exception {
        when(backendService.getrandomSpecies()).thenReturn(species());

        mockMvc.perform(get("/api/v1/getspecies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Panthera leo"));
    }

    @Test
    void returnsSpeciesDetailsById() throws Exception {
        when(speciesRepository.findBySpeciesId("Panthera leo"))
                .thenReturn(Optional.of(species()));

        mockMvc.perform(get("/api/v1/species/{id}", "Panthera leo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data")
                        .value(org.hamcrest.Matchers.containsString("ID: Panthera leo")))
                .andExpect(jsonPath("$.data")
                        .value(org.hamcrest.Matchers.containsString("Common Name: Lion")));
    }

    @Test
    void returnsNotFoundForMissingSpecies() throws Exception {
        when(speciesRepository.findBySpeciesId("Missing species"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/species/{id}", "Missing species"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Species not found with id: Missing species"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private Species species() {
        return new Species(
                "Panthera leo",
                "Lion",
                false,
                new Genus("Panthera", null));
    }
}
