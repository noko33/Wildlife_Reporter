package com.wildlifedb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "idx_species_common_name", columnList = "common_name"),
    @Index(name = "idx_species_genus", columnList = "genus_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Species {
    @Id
    private String speciesId;

    private String commonName;

    private Boolean extinctStatus;

    @ManyToOne
    @JoinColumn(name="genusId")
    private Genus genus;
}
