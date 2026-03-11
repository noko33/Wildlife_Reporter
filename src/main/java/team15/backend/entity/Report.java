package team15.backend.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
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
