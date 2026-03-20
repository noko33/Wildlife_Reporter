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
public class Family {
    @Id
    private String familyId;

    @ManyToOne
    @JoinColumn(name="orderId")
    private TaxonomyOrder taxOrder;
}
