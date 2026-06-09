package com.wildlifedb.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.CreateObservationRequest;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.dto.UpdateObservationRequest;
import com.wildlifedb.entity.Location;
import com.wildlifedb.entity.Report;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.User;
import com.wildlifedb.exception.ResourceNotFoundException;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.ReportRepository;
import com.wildlifedb.repository.ReportSpecifications;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.UserRepository;

@Service
public class ObservationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReportRepository reportRepository;
    private final SpeciesRepository speciesRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public ObservationService(
            ReportRepository reportRepository,
            SpeciesRepository speciesRepository,
            LocationRepository locationRepository,
            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.speciesRepository = speciesRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public ObservationResponse createObservation(
            String authenticatedEmail,
            CreateObservationRequest request) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Species species = findSpecies(request.speciesName());

        Report report = new Report();
        report.setUser(user);
        report.setSpecies(species);
        report.setLocation(findLocation(request.locationId()));
        report.setDateTime(request.observedAt() == null
                ? OffsetDateTime.now()
                : request.observedAt());
        report.setComment(request.comment());
        report.setAgeApproximation(request.ageApproximation());
        report.setLongitude(request.longitude());
        report.setLatitude(request.latitude());
        report.setVerified(Boolean.TRUE.equals(user.getVerifier()));
        if (Boolean.TRUE.equals(user.getVerifier())) {
            report.setVerifierUser(user);
        }

        return ObservationResponse.from(reportRepository.save(report));
    }

    @Transactional
    public ObservationResponse updateObservation(
            int observationId,
            String authenticatedEmail,
            UpdateObservationRequest request) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Report report = findObservation(observationId);
        verifyCanModify(user, report);

        if (StringUtils.hasText(request.speciesName())) {
            report.setSpecies(findSpecies(request.speciesName()));
        }
        if (request.locationId() != null) {
            report.setLocation(findLocation(request.locationId()));
        }
        if (request.observedAt() != null) {
            report.setDateTime(request.observedAt());
        }
        if (request.comment() != null) {
            report.setComment(request.comment());
        }
        if (request.ageApproximation() != null) {
            report.setAgeApproximation(request.ageApproximation());
        }
        if (request.longitude() != null) {
            report.setLongitude(request.longitude());
        }
        if (request.latitude() != null) {
            report.setLatitude(request.latitude());
        }

        return ObservationResponse.from(reportRepository.save(report));
    }

    @Transactional
    public void deleteObservation(int observationId, String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Report report = findObservation(observationId);
        verifyCanModify(user, report);
        reportRepository.delete(report);
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

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user was not found"));
    }

    private Report findObservation(int observationId) {
        return reportRepository.findByReportId(observationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Observation not found with id: " + observationId));
    }

    private Species findSpecies(String speciesName) {
        return speciesRepository.findBySpeciesId(speciesName.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Species not found with name: " + speciesName));
    }

    private Location findLocation(Integer locationId) {
        if (locationId == null) {
            return null;
        }
        return locationRepository.findByLocationId(locationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id: " + locationId));
    }

    private void verifyCanModify(User user, Report report) {
        boolean owner = report.getUser() != null
                && report.getUser().getId().equals(user.getId());
        if (!owner && !Boolean.TRUE.equals(user.getVerifier())) {
            throw new AccessDeniedException(
                    "You do not have permission to modify this observation");
        }
    }
}
