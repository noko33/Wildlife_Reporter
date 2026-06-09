package com.wildlifedb.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "idx_report_date_id", columnList = "date_time, report_id"),
    @Index(
            name = "idx_report_species_date_id",
            columnList = "species, date_time, report_id"),
    @Index(
            name = "idx_report_location_date_id",
            columnList = "location_id, date_time, report_id"),
    @Index(
            name = "idx_report_user_date_id",
            columnList = "user_id, date_time, report_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int reportId;

    @ManyToOne //Many report.userId to One User.id
    @JoinColumn(name="userId")
    private User user;

    @OneToOne
    @JoinColumn(name="imageId")
    private Image image;

    @ManyToOne
    @JoinColumn(name="species")
    private Species species;

    private Boolean verified;

    @ManyToOne
    @JoinColumn(name="verifierId")
    private User verifierUser;

    private String comment;

    private Integer ageApproximation;

    private OffsetDateTime dateTime;

    private Float longitude;

    private Float latitude;

    @ManyToOne
    @JoinColumn(name="locationId")
    private Location location;
}
