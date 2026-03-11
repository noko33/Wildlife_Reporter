package team15.backend.entity;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer locationId;

    @Column(columnDefinition = "LONGTEXT")
    private String geometricShape; // Store as WKT string

    private String state;

    private String city;

    private String biome;
    
    // Helper methods to convert between Geometry and WKT
    @Transient
    public Geometry getGeometry() {
        if (geometricShape == null || geometricShape.isEmpty()) {
            return null;
        }
        try {
            WKTReader reader = new WKTReader();
            return reader.read(geometricShape);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Transient
    public void setGeometry(Geometry geometry) {
        if (geometry == null) {
            this.geometricShape = null;
        } else {
            WKTWriter writer = new WKTWriter();
            this.geometricShape = writer.write(geometry);
        }
    }
}