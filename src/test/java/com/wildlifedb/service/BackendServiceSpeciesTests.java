package com.wildlifedb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Species;
import com.wildlifedb.repository.FamilyRepository;
import com.wildlifedb.repository.GenusRepository;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.OrderRepository;
import com.wildlifedb.repository.PhylumRepository;
import com.wildlifedb.repository.ReportRepository;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.TaxonomyClassRepository;
import com.wildlifedb.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BackendServiceSpeciesTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SpeciesRepository speciesRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private GenusRepository genusRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PhylumRepository phylumRepository;
    @Mock
    private TaxonomyClassRepository taxonomyClassRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BackendService backendService;

    @Test
    void returnsRandomSpeciesFromAvailableRecords() {
        Species lion = species();
        when(speciesRepository.findAll()).thenReturn(List.of(lion));

        Species result = backendService.getrandomSpecies();

        assertThat(result).isSameAs(lion);
    }

    @Test
    void addsSpeciesWithSelectedGenus() {
        Genus genus = new Genus("Panthera", null);

        backendService.addSpecies("Panthera leo", genus);

        ArgumentCaptor<Species> captor = ArgumentCaptor.forClass(Species.class);
        verify(speciesRepository).save(captor.capture());
        Species saved = captor.getValue();
        assertThat(saved.getSpeciesId()).isEqualTo("Panthera leo");
        assertThat(saved.getGenus()).isSameAs(genus);
    }

    @Test
    void updatesExistingSpecies() {
        Species lion = species();
        when(speciesRepository.findBySpeciesId("Panthera leo"))
                .thenReturn(Optional.of(lion));

        backendService.updateSpecies("Panthera leo", "Asiatic lion", true);

        assertThat(lion.getCommonName()).isEqualTo("Asiatic lion");
        assertThat(lion.getExtinctStatus()).isTrue();
        verify(speciesRepository).save(lion);
    }

    @Test
    void doesNotSaveWhenSpeciesToUpdateIsMissing() {
        when(speciesRepository.findBySpeciesId("Missing species"))
                .thenReturn(Optional.empty());

        backendService.updateSpecies("Missing species", "Unknown", false);

        verify(speciesRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Species.class));
    }

    private Species species() {
        return new Species(
                "Panthera leo",
                "Lion",
                false,
                new Genus("Panthera", null));
    }
}
