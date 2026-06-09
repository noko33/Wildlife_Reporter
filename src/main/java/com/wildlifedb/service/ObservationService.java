package com.wildlifedb.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.entity.Report;
import com.wildlifedb.repository.ReportRepository;
import com.wildlifedb.repository.ReportSpecifications;

@Service
public class ObservationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReportRepository reportRepository;

    public ObservationService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ObservationResponse> findObservations(
            int page,
            int size,
            String speciesName,
            String location,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String taxonomy) {
        validatePagination(page, size);
        validateDateRange(startDate, endDate);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("dateTime"),
                        Sort.Order.desc("reportId")));

        Page<Report> reports = reportRepository.findAll(
                ReportSpecifications.withFilters(
                        speciesName,
                        location,
                        startDate,
                        endDate,
                        taxonomy),
                pageable);

        return PageResponse.from(reports, ObservationResponse::from);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void validateDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }
    }
}
