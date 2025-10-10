package com.tushar.geotrackr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "geofences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Geofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private GeofenceType type = GeofenceType.CIRCULAR;

    // For circular geofences
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radius; // in meters

    // For polygonal geofences (stored as JSON or separate table)
    @Column(columnDefinition = "TEXT")
    private String polygonCoordinates; // JSON format: [[lat,lon],[lat,lon],...]

    @Enumerated(EnumType.STRING)
    private AlertType alertType = AlertType.BOTH;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private com.tushar.geotrackr.entity.User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "geofence", cascade = CascadeType.ALL)
    private Set<com.tushar.geotrackr.entity.GeofenceEvent> geofenceEvents = new HashSet<>();

    public enum GeofenceType {
        CIRCULAR, POLYGONAL
    }

    public enum AlertType {
        ENTRY, EXIT, BOTH
    }
}
