package com.wildlifedb.controller;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wildlifedb.api.ApiResponse;
import com.wildlifedb.api.PageResponse;
import com.wildlifedb.config.OpenApiConfig;
import com.wildlifedb.dto.CreateObservationRequest;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.dto.UpdateObservationRequest;
import com.wildlifedb.service.ObservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/observations", "/observations"})
@Tag(
        name = "Observations",
        description = "Search and manage wildlife observation records")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping
    @Operation(
            summary = "Search observations",
            description = "Returns a paginated list. All filters are optional and this endpoint is public.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Observation page returned, including an empty page when no records match"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid pagination, date range, or query parameter"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Unexpected server or database error")
    })
    public ApiResponse<PageResponse<ObservationResponse>> getObservations(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(
                    description = "Scientific or common species name, matched case-insensitively",
                    example = "Panthera")
            @RequestParam(required = false) String speciesName,
            @Parameter(
                    description = "City, state, or biome text",
                    example = "Illinois")
            @RequestParam(required = false) String location,
            @Parameter(
                    description = "Inclusive observation start time in ISO-8601 format",
                    example = "2025-01-01T00:00:00Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,
            @Parameter(
                    description = "Inclusive observation end time in ISO-8601 format",
                    example = "2025-12-31T23:59:59Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate,
            @Parameter(
                    description = "Genus, family, order, class, phylum, or kingdom text",
                    example = "Mammalia")
            @RequestParam(required = false) String taxonomy,
            @Parameter(
                    description = "Alias for taxonomy; ignored when taxonomy is provided",
                    example = "Mammalia")
            @RequestParam(required = false) String category) {
        String taxonomyFilter = StringUtils.hasText(taxonomy) ? taxonomy : category;
        PageResponse<ObservationResponse> result = observationService.findObservations(
                page,
                size,
                speciesName,
                location,
                startDate,
                endDate,
                taxonomyFilter);
        return ApiResponse.success(result);
    }

    @PostMapping
    @Operation(
            summary = "Create an observation",
            description = "Creates an observation owned by the authenticated user.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Observation created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid observation data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "JWT is missing, invalid, or expired"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Species, location, or authenticated user not found")
    })
    public ApiResponse<ObservationResponse> createObservation(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody CreateObservationRequest request) {
        return ApiResponse.success(
                "Observation created successfully",
                observationService.createObservation(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an observation",
            description = "Updates an observation owned by the authenticated user. Verifiers may update any observation.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Observation updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid observation data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "JWT is missing, invalid, or expired"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Authenticated user cannot modify this observation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Observation, species, or location not found")
    })
    public ApiResponse<ObservationResponse> updateObservation(
            @Parameter(description = "Observation ID", example = "42")
            @PathVariable int id,
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody UpdateObservationRequest request) {
        return ApiResponse.success(
                "Observation updated successfully",
                observationService.updateObservation(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an observation",
            description = "Deletes an observation owned by the authenticated user. Verifiers may delete any observation.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Observation deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "JWT is missing, invalid, or expired"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Authenticated user cannot delete this observation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Observation not found")
    })
    public ApiResponse<Void> deleteObservation(
            @Parameter(description = "Observation ID", example = "42")
            @PathVariable int id,
            @Parameter(hidden = true)
            Authentication authentication) {
        observationService.deleteObservation(id, authentication.getName());
        return ApiResponse.success("Observation deleted successfully", null);
    }
}
