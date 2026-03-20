package com.wildlifedb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
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
