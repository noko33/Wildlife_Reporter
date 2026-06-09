package com.wildlifedb.dto;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateObservationRequest(
        @Schema(
                description = "Existing scientific species ID",
                example = "Panthera leo")
        @NotBlank(message = "speciesName is required")
        String speciesName,

        @Schema(description = "Existing location ID", example = "15", nullable = true)
        Integer locationId,

        @Schema(
                description = "Observation time in ISO-8601 format; defaults to the current time",
                example = "2025-06-15T14:30:00Z",
                nullable = true)
        OffsetDateTime observedAt,

        @Schema(
                description = "Optional observation note",
                example = "Adult animal near the tree line",
                maxLength = 255,
                nullable = true)
        @Size(max = 255, message = "comment must not exceed 255 characters")
        String comment,

        @Schema(description = "Approximate age in years", example = "4", minimum = "0", nullable = true)
        @PositiveOrZero(message = "ageApproximation must be greater than or equal to 0")
        Integer ageApproximation,

        @Schema(description = "Longitude in decimal degrees", example = "-88.2272", nullable = true)
        @DecimalMin(value = "-180.0", message = "longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "longitude must be at most 180")
        Float longitude,

        @Schema(description = "Latitude in decimal degrees", example = "40.1106", nullable = true)
        @DecimalMin(value = "-90.0", message = "latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "latitude must be at most 90")
        Float latitude) {
}
