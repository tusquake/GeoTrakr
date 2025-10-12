package com.tushar.geotrackr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Enhanced location update with GPS metadata
 * Use this for detailed GPS tracking with quality metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedLocationUpdateDTO {

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private LocalDateTime timestamp;

    // GPS Quality Metrics
    private Double altitude;        // meters above sea level
    private Double speed;           // km/h
    private Double heading;         // degrees (0-360)
    private Double accuracy;        // meters (horizontal accuracy)
    private Integer satellites;     // number of satellites in view
    private Double hdop;            // Horizontal Dilution of Precision
    private String fixQuality;      // NO_FIX, GPS_FIX, DGPS_FIX, etc.
    private String provider;        // GPS, NETWORK, FUSED

    // Device Information
    private String deviceId;        // Unique device identifier
    private Double batteryLevel;    // 0-100 percentage
    private String networkType;     // 4G, 5G, WIFI, etc.

    /**
     * Check if GPS fix is reliable
     * @return true if GPS data is reliable
     */
    public boolean isReliableFix() {
        // Consider fix reliable if:
        // - Has at least 4 satellites
        // - Accuracy better than 20 meters
        // - HDOP < 2.0 (good precision)
        return satellites != null && satellites >= 4
                && accuracy != null && accuracy <= 20.0
                && hdop != null && hdop < 2.0;
    }

    /**
     * Get signal strength category
     * @return EXCELLENT, GOOD, FAIR, POOR, NO_SIGNAL
     */
    public String getSignalStrength() {
        if (satellites == null || satellites == 0) {
            return "NO_SIGNAL";
        } else if (satellites >= 8 && accuracy != null && accuracy < 5) {
            return "EXCELLENT";
        } else if (satellites >= 6 && accuracy != null && accuracy < 10) {
            return "GOOD";
        } else if (satellites >= 4 && accuracy != null && accuracy < 20) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
}