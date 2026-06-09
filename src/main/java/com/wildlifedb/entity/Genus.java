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
    @Index(name = "idx_genus_family", columnList = "family_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Genus {
    @Id
    private String genusId;

    @ManyToOne
    @JoinColumn(name="familyId")
    private Family family;
}
