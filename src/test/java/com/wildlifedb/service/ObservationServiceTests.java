package com.wildlifedb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import com.wildlifedb.api.PageResponse;
import com.wildlifedb.dto.CreateObservationRequest;
import com.wildlifedb.dto.ObservationResponse;
import com.wildlifedb.dto.UpdateObservationRequest;
import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Location;
import com.wildlifedb.entity.Report;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.User;
import com.wildlifedb.exception.ResourceNotFoundException;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.ReportRepository;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.UserRepository;

class ObservationServiceTests {

    private ReportRepository reportRepository;
    private SpeciesRepository speciesRepository;
    private LocationRepository locationRepository;
    private UserRepository userRepository;
    private ObservationService observationService;

    @BeforeEach
    void setUp() {
        reportRepository = mock(ReportRepository.class);
        speciesRepository = mock(SpeciesRepository.class);
        locationRepository = mock(LocationRepository.class);
        userRepository = mock(UserRepository.class);
        observationService = new ObservationService(
                reportRepository,
                speciesRepository,
                locationRepository,
                userRepository);
    }

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

    @Test
    void returnsRequestedObservationPageWithMappedItems() {
        Report report = report(
                41,
                owner(),
                species(),
                location(),
                OffsetDateTime.parse("2026-05-12T08:30:00Z"));
        Page<Report> reports = new PageImpl<>(
                List.of(report),
                PageRequest.of(2, 5),
                11);
        when(reportRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Report>>any(),
                any(Pageable.class)))
                .thenReturn(reports);

        PageResponse<ObservationResponse> result = observationService.findObservations(
                2,
                5,
                "Panthera leo",
                "Springfield",
                OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                OffsetDateTime.parse("2026-05-31T23:59:59Z"),
                "Felidae");

        assertThat(result.getTotalElements()).isEqualTo(11);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getItems()).singleElement()
                .extracting(ObservationResponse::speciesName)
                .isEqualTo("Panthera leo");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Report>>any(),
                pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("dateTime")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("dateTime").isDescending()).isTrue();
    }

    @Test
    void createsObservationForAuthenticatedUser() {
        User owner = owner();
        Species species = species();
        Location location = location();
        OffsetDateTime observedAt = OffsetDateTime.parse("2026-05-12T08:30:00Z");
        CreateObservationRequest request = new CreateObservationRequest(
                species.getSpeciesId(),
                location.getLocationId(),
                observedAt,
                "Adult animal near the river",
                4,
                -89.65f,
                39.78f);
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(speciesRepository.findBySpeciesId(species.getSpeciesId()))
                .thenReturn(Optional.of(species));
        when(locationRepository.findByLocationId(location.getLocationId()))
                .thenReturn(Optional.of(location));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            saved.setReportId(101);
            return saved;
        });

        ObservationResponse result = observationService.createObservation(
                owner.getEmail(),
                request);

        assertThat(result.id()).isEqualTo(101);
        assertThat(result.observerUserId()).isEqualTo("user-1");
        assertThat(result.speciesName()).isEqualTo("Panthera leo");
        assertThat(result.city()).isEqualTo("Springfield");
        assertThat(result.observedAt()).isEqualTo(observedAt);
        assertThat(result.verified()).isFalse();

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        Report saved = reportCaptor.getValue();
        assertThat(saved.getUser()).isSameAs(owner);
        assertThat(saved.getSpecies()).isSameAs(species);
        assertThat(saved.getLocation()).isSameAs(location);
        assertThat(saved.getComment()).isEqualTo("Adult animal near the river");
    }

    @Test
    void rejectsCreationWhenSpeciesDoesNotExist() {
        User owner = owner();
        CreateObservationRequest request = new CreateObservationRequest(
                "Missing species",
                7,
                OffsetDateTime.now(),
                null,
                null,
                null,
                null);
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(speciesRepository.findBySpeciesId("Missing species"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.createObservation(
                owner.getEmail(),
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Species");
        verify(reportRepository, never()).save(any());
    }

    @Test
    void rejectsCreationWhenLocationDoesNotExist() {
        User owner = owner();
        Species species = species();
        CreateObservationRequest request = new CreateObservationRequest(
                species.getSpeciesId(),
                999,
                OffsetDateTime.now(),
                null,
                null,
                null,
                null);
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(speciesRepository.findBySpeciesId(species.getSpeciesId()))
                .thenReturn(Optional.of(species));
        when(locationRepository.findByLocationId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.createObservation(
                owner.getEmail(),
                request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location");
        verify(reportRepository, never()).save(any());
    }

    @Test
    void updatesObservationOwnedByAuthenticatedUser() {
        User owner = owner();
        Report existing = report(
                41,
                owner,
                species(),
                location(),
                OffsetDateTime.parse("2026-05-10T08:30:00Z"));
        Species replacementSpecies = new Species(
                "Loxodonta africana",
                "African elephant",
                false,
                new Genus("Loxodonta", null));
        Location replacementLocation = location();
        replacementLocation.setLocationId(8);
        replacementLocation.setCity("Chicago");
        OffsetDateTime replacementDate = OffsetDateTime.parse("2026-05-15T10:00:00Z");
        UpdateObservationRequest request = new UpdateObservationRequest(
                replacementSpecies.getSpeciesId(),
                replacementLocation.getLocationId(),
                replacementDate,
                "Updated sighting",
                8,
                -87.63f,
                41.88f);
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(reportRepository.findByReportId(41)).thenReturn(Optional.of(existing));
        when(speciesRepository.findBySpeciesId(replacementSpecies.getSpeciesId()))
                .thenReturn(Optional.of(replacementSpecies));
        when(locationRepository.findByLocationId(replacementLocation.getLocationId()))
                .thenReturn(Optional.of(replacementLocation));
        when(reportRepository.save(existing)).thenReturn(existing);

        ObservationResponse result = observationService.updateObservation(
                41,
                owner.getEmail(),
                request);

        assertThat(result.speciesName()).isEqualTo("Loxodonta africana");
        assertThat(result.city()).isEqualTo("Chicago");
        assertThat(result.comment()).isEqualTo("Updated sighting");
        assertThat(result.observedAt()).isEqualTo(replacementDate);
        verify(reportRepository).save(existing);
    }

    @Test
    void rejectsUpdateWhenObservationDoesNotExist() {
        User owner = owner();
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(reportRepository.findByReportId(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.updateObservation(
                404,
                owner.getEmail(),
                new UpdateObservationRequest(null, null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Observation");
    }

    @Test
    void rejectsUpdateByAnotherNonVerifierUser() {
        User owner = owner();
        User otherUser = new User(2, "user-2", "other@example.com", "hash", false);
        Report existing = report(41, owner, species(), location(), OffsetDateTime.now());
        when(userRepository.findByEmailIgnoreCase(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(reportRepository.findByReportId(41)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> observationService.updateObservation(
                41,
                otherUser.getEmail(),
                new UpdateObservationRequest(
                        null,
                        null,
                        null,
                        "Unauthorized edit",
                        null,
                        null,
                        null)))
                .isInstanceOf(AccessDeniedException.class);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void deletesObservationOwnedByAuthenticatedUser() {
        User owner = owner();
        Report existing = report(41, owner, species(), location(), OffsetDateTime.now());
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(reportRepository.findByReportId(41)).thenReturn(Optional.of(existing));

        observationService.deleteObservation(41, owner.getEmail());

        verify(reportRepository).delete(existing);
    }

    @Test
    void rejectsDeleteWhenObservationDoesNotExist() {
        User owner = owner();
        when(userRepository.findByEmailIgnoreCase(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(reportRepository.findByReportId(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> observationService.deleteObservation(
                404,
                owner.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Observation");
        verify(reportRepository, never()).delete(any(Report.class));
    }

    private User owner() {
        return new User(1, "user-1", "owner@example.com", "hash", false);
    }

    private Species species() {
        return new Species(
                "Panthera leo",
                "Lion",
                false,
                new Genus("Panthera", null));
    }

    private Location location() {
        Location location = new Location();
        location.setLocationId(7);
        location.setCity("Springfield");
        location.setState("IL");
        location.setBiome("Grassland");
        return location;
    }

    private Report report(
            int id,
            User user,
            Species species,
            Location location,
            OffsetDateTime observedAt) {
        Report report = new Report();
        report.setReportId(id);
        report.setUser(user);
        report.setSpecies(species);
        report.setLocation(location);
        report.setDateTime(observedAt);
        report.setComment("Original sighting");
        report.setVerified(false);
        return report;
    }
}
