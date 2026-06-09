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
    @Index(name = "idx_taxonomy_class_phylum", columnList = "phylum_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxonomyClass {
    @Id
    private String classId;

    @ManyToOne
    @JoinColumn(name="phylumId")
    private Phylum phylum;
}
