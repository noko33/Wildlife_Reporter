package com.wildlifedb.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.wildlifedb.entity.Family;
import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Location;
import com.wildlifedb.entity.Phylum;
import com.wildlifedb.entity.Report;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.TaxonomyClass;
import com.wildlifedb.entity.TaxonomyOrder;
import com.wildlifedb.entity.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ReportRepositorySpecificationTests {

    private static final OffsetDateTime BASE_TIME =
            OffsetDateTime.of(2026, 1, 10, 12, 0, 0, 0, ZoneOffset.UTC);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        Species lion = createSpecies(
                "Panthera leo",
                "Lion",
                "Panthera",
                "Felidae",
                "Carnivora",
                "Mammalia",
                "Chordata",
                "Animalia");
        Species eagle = createSpecies(
                "Aquila chrysaetos",
                "Golden Eagle",
                "Aquila",
                "Accipitridae",
                "Accipitriformes",
                "Aves",
                "ChordataBird",
                "Animalia");

        Location springfield = createLocation("Springfield", "IL", "Grassland");
        Location denver = createLocation("Denver", "CO", "Mountain");

        User observer = new User();
        observer.setUserId("observer");
        observer.setEmail("observer@example.com");
        observer.setPassword("password");
        observer.setVerifier(false);
        entityManager.persist(observer);

        entityManager.persist(createReport(observer, lion, springfield, BASE_TIME.minusDays(2)));
        entityManager.persist(createReport(observer, lion, denver, BASE_TIME));
        entityManager.persist(createReport(observer, eagle, springfield, BASE_TIME.plusDays(2)));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void appliesCombinedFilters() {
        Page<Report> result = reportRepository.findAll(
                ReportSpecifications.withFilters(
                        "lion",
                        "spring",
                        BASE_TIME.minusDays(3),
                        BASE_TIME.minusDays(1),
                        "felidae"),
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getSpecies().getSpeciesId())
                .isEqualTo("Panthera leo");
        assertThat(result.getContent().get(0).getLocation().getCity())
                .isEqualTo("Springfield");
    }

    @Test
    void paginatesWhenFiltersAreEmpty() {
        Page<Report> result = reportRepository.findAll(
                ReportSpecifications.withFilters(null, " ", null, null, null),
                PageRequest.of(1, 2));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    private Species createSpecies(
            String speciesId,
            String commonName,
            String genusId,
            String familyId,
            String orderId,
            String classId,
            String phylumId,
            String kingdomId) {
        Phylum phylum = new Phylum(phylumId, kingdomId);
        entityManager.persist(phylum);

        TaxonomyClass taxonomyClass = new TaxonomyClass(classId, phylum);
        entityManager.persist(taxonomyClass);

        TaxonomyOrder taxonomyOrder = new TaxonomyOrder(orderId, taxonomyClass);
        entityManager.persist(taxonomyOrder);

        Family family = new Family(familyId, taxonomyOrder);
        entityManager.persist(family);

        Genus genus = new Genus(genusId, family);
        entityManager.persist(genus);

        Species species = new Species(speciesId, commonName, false, genus);
        entityManager.persist(species);
        return species;
    }

    private Location createLocation(String city, String state, String biome) {
        Location location = new Location();
        location.setCity(city);
        location.setState(state);
        location.setBiome(biome);
        entityManager.persist(location);
        return location;
    }

    private Report createReport(
            User observer,
            Species species,
            Location location,
            OffsetDateTime dateTime) {
        Report report = new Report();
        report.setUser(observer);
        report.setSpecies(species);
        report.setLocation(location);
        report.setDateTime(dateTime);
        report.setVerified(false);
        return report;
    }
}
