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
import com.wildlifedb.dto.CreateObservationRequest;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.dto.UpdateObservationRequest;
import com.wildlifedb.service.ObservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/observations", "/observations"})
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ObservationResponse>> getObservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String speciesName,
            @RequestParam(required = false) String location,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate,
            @RequestParam(required = false) String taxonomy,
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
    public ApiResponse<ObservationResponse> createObservation(
            Authentication authentication,
            @Valid @RequestBody CreateObservationRequest request) {
        return ApiResponse.success(
                "Observation created successfully",
                observationService.createObservation(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ObservationResponse> updateObservation(
            @PathVariable int id,
            Authentication authentication,
            @Valid @RequestBody UpdateObservationRequest request) {
        return ApiResponse.success(
                "Observation updated successfully",
                observationService.updateObservation(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteObservation(
            @PathVariable int id,
            Authentication authentication) {
        observationService.deleteObservation(id, authentication.getName());
        return ApiResponse.success("Observation deleted successfully", null);
    }
}
