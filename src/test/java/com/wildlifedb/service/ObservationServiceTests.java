package com.wildlifedb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.entity.Report;
import com.wildlifedb.repository.ReportRepository;

class ObservationServiceTests {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final ObservationService observationService =
            new ObservationService(reportRepository);

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> observationService.findObservations(
                -1, 20, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page must be greater than or equal to 0");
    }

    @Test
    void rejectsPageSizeOutsideAllowedRange() {
        assertThatThrownBy(() -> observationService.findObservations(
                0, 0, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");

        assertThatThrownBy(() -> observationService.findObservations(
                0, 101, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }

    @Test
    void rejectsInvertedDateRange() {
        OffsetDateTime startDate = OffsetDateTime.parse("2026-02-01T00:00:00Z");
        OffsetDateTime endDate = OffsetDateTime.parse("2026-01-01T00:00:00Z");

        assertThatThrownBy(() -> observationService.findObservations(
                0, 20, null, null, startDate, endDate, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must be before or equal to endDate");
    }

    @Test
    void returnsEmptyPageWithoutTreatingItAsAnError() {
        Pageable requestedPage = PageRequest.of(3, 5);
        when(reportRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Report>>any(),
                any(Pageable.class)))
                .thenReturn(Page.empty(requestedPage));

        PageResponse<ObservationResponse> result = observationService.findObservations(
                3, 5, null, null, null, null, null);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getPage()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getItems()).isEmpty();
    }
}
