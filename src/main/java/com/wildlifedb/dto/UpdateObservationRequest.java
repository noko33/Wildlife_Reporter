package com.wildlifedb.dto;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateObservationRequest(
        @Schema(description = "Replacement scientific species ID", example = "Panthera leo")
        String speciesName,

        @Schema(description = "Replacement location ID", example = "15")
        Integer locationId,

        @Schema(
                description = "Replacement observation time in ISO-8601 format",
                example = "2025-06-15T14:30:00Z")
        OffsetDateTime observedAt,

        @Schema(description = "Replacement observation note", maxLength = 255)
        @Size(max = 255, message = "comment must not exceed 255 characters")
        String comment,

        @Schema(description = "Replacement approximate age in years", minimum = "0")
        @PositiveOrZero(message = "ageApproximation must be greater than or equal to 0")
        Integer ageApproximation,

        @Schema(description = "Replacement longitude in decimal degrees", example = "-88.2272")
        @DecimalMin(value = "-180.0", message = "longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "longitude must be at most 180")
        Float longitude,

        @Schema(description = "Replacement latitude in decimal degrees", example = "40.1106")
        @DecimalMin(value = "-90.0", message = "latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "latitude must be at most 90")
        Float latitude) {
}
