package com.wildlifedb.dto;

import java.time.OffsetDateTime;

import com.wildlifedb.entity.Family;
import com.wildlifedb.entity.Genus;
import com.wildlifedb.entity.Location;
import com.wildlifedb.entity.Phylum;
import com.wildlifedb.entity.Report;
import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.TaxonomyClass;
import com.wildlifedb.entity.TaxonomyOrder;

public record ObservationResponse(
        int id,
        String observerUserId,
        String speciesName,
        String commonName,
        String genus,
        String family,
        String taxonomyOrder,
        String taxonomyClass,
        String phylum,
        String kingdom,
        Boolean verified,
        String comment,
        Integer ageApproximation,
        OffsetDateTime observedAt,
        Float longitude,
        Float latitude,
        String city,
        String state,
        String biome) {

    public static ObservationResponse from(Report report) {
        Species species = report.getSpecies();
        Genus genus = species == null ? null : species.getGenus();
        Family family = genus == null ? null : genus.getFamily();
        TaxonomyOrder order = family == null ? null : family.getTaxOrder();
        TaxonomyClass taxonomyClass = order == null ? null : order.getTaxonomyClass();
        Phylum phylum = taxonomyClass == null ? null : taxonomyClass.getPhylum();
        Location location = report.getLocation();

        return new ObservationResponse(
                report.getReportId(),
                report.getUser() == null ? null : report.getUser().getUserId(),
                species == null ? null : species.getSpeciesId(),
                species == null ? null : species.getCommonName(),
                genus == null ? null : genus.getGenusId(),
                family == null ? null : family.getFamilyId(),
                order == null ? null : order.getOrderId(),
                taxonomyClass == null ? null : taxonomyClass.getClassId(),
                phylum == null ? null : phylum.getPhylumId(),
                phylum == null ? null : phylum.getKingdomId(),
                report.getVerified(),
                report.getComment(),
                report.getAgeApproximation(),
                report.getDateTime(),
                report.getLongitude(),
                report.getLatitude(),
                location == null ? null : location.getCity(),
                location == null ? null : location.getState(),
                location == null ? null : location.getBiome());
    }
}
