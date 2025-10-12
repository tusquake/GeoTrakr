package com.tushar.geotrackr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing GPS metadata and quality information
 * Optional - use if you want to track GPS signal quality
 */
@Entity
@Table(name = "gps_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPSData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude; // meters

    @Column(name = "speed")
    private Double speed; // km/h

    @Column(name = "heading")
    private Double heading; // degrees (0-360), direction of travel

    @Column(name = "accuracy")
    private Double accuracy; // meters, horizontal accuracy

    @Column(name = "satellites")
    private Integer satellites; // number of GPS satellites

    @Column(name = "hdop")
    private Double hdop; // Horizontal Dilution of Precision (GPS quality metric)

    @Enumerated(EnumType.STRING)
    @Column(name = "fix_quality")
    private FixQuality fixQuality;

    @Column(name = "provider")
    private String provider; // GPS, NETWORK, FUSED, etc.

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public enum FixQuality {
        NO_FIX,        // No GPS signal
        GPS_FIX,       // Standard GPS fix
        DGPS_FIX,      // Differential GPS (more accurate)
        PPS_FIX,       // Military-grade precision
        RTK_FIX,       // Real-Time Kinematic (centimeter accuracy)
        ESTIMATED      // Dead reckoning estimate
    }
}