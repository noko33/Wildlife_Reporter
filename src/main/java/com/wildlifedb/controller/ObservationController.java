package com.wildlifedb.controller;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wildlifedb.api.ApiResponse;
import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.service.ObservationService;

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
}
