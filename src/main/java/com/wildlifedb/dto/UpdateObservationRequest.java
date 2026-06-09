package com.wildlifedb.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateObservationRequest(
        String speciesName,

        Integer locationId,

        OffsetDateTime observedAt,

        @Size(max = 255, message = "comment must not exceed 255 characters")
        String comment,

        @PositiveOrZero(message = "ageApproximation must be greater than or equal to 0")
        Integer ageApproximation,

        @DecimalMin(value = "-180.0", message = "longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "longitude must be at most 180")
        Float longitude,

        @DecimalMin(value = "-90.0", message = "latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "latitude must be at most 90")
        Float latitude) {
}
