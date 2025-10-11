package com.tushar.geotrackr.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 👈 Prevent ByteBuddy issues
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private GeofenceType type = GeofenceType.CIRCULAR;

    private Double centerLatitude;
    private Double centerLongitude;
    private Double radius;

    @Column(columnDefinition = "TEXT")
    private String polygonCoordinates;

    @Enumerated(EnumType.STRING)
    private AlertType alertType = AlertType.BOTH;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore // 👈 avoid serializing the user proxy
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "geofence", cascade = CascadeType.ALL)
    @JsonIgnore // 👈 prevent infinite recursion
    private Set<GeofenceEvent> geofenceEvents = new HashSet<>();

    public enum GeofenceType {
        CIRCULAR, POLYGONAL
    }

    public enum AlertType {
        ENTRY, EXIT, BOTH
    }
}
