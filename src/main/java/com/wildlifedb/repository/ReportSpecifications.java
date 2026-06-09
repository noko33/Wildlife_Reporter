package com.wildlifedb.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.wildlifedb.entity.Family;
import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Location;
import com.wildlifedb.entity.Phylum;
import com.wildlifedb.entity.Report;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.TaxonomyClass;
import com.wildlifedb.entity.TaxonomyOrder;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    public static Specification<Report> withFilters(
            String speciesName,
            String location,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String taxonomy) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Report, Species> speciesJoin = null;
            if (StringUtils.hasText(speciesName) || StringUtils.hasText(taxonomy)) {
                speciesJoin = root.join("species", JoinType.INNER);
            }

            if (StringUtils.hasText(speciesName)) {
                String pattern = containsPattern(speciesName);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(speciesJoin.get("speciesId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(speciesJoin.get("commonName")),
                                pattern,
                                '\\')));
            }

            if (StringUtils.hasText(location)) {
                String pattern = containsPattern(location);
                Join<Report, Location> locationJoin = root.join("location", JoinType.INNER);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(locationJoin.get("city")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(locationJoin.get("state")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(locationJoin.get("biome")),
                                pattern,
                                '\\')));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateTime"),
                        startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateTime"),
                        endDate));
            }

            if (StringUtils.hasText(taxonomy)) {
                String pattern = containsPattern(taxonomy);
                Join<Species, Genus> genusJoin = speciesJoin.join("genus", JoinType.INNER);
                Join<Genus, Family> familyJoin = genusJoin.join("family", JoinType.INNER);
                Join<Family, TaxonomyOrder> orderJoin = familyJoin.join("taxOrder", JoinType.INNER);
                Join<TaxonomyOrder, TaxonomyClass> classJoin =
                        orderJoin.join("taxonomyClass", JoinType.INNER);
                Join<TaxonomyClass, Phylum> phylumJoin =
                        classJoin.join("phylum", JoinType.INNER);

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(genusJoin.get("genusId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(familyJoin.get("familyId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(orderJoin.get("orderId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(classJoin.get("classId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(phylumJoin.get("phylumId")),
                                pattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(phylumJoin.get("kingdomId")),
                                pattern,
                                '\\')));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String containsPattern(String value) {
        String escaped = value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
