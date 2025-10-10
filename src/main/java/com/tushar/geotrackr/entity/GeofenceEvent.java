package com.tushar.geotrackr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "geofence_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geofence_id", nullable = false)
    private Geofence geofence;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Double latitude;
    private Double longitude;

    @CreationTimestamp
    private LocalDateTime timestamp;

    private boolean notificationSent = false;

    public enum EventType {
        ENTER, EXIT
    }
}
