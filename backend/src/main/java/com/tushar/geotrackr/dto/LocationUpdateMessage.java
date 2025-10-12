package com.tushar.geotrackr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Real-time location update message for WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateMessage {
    private Long assetId;
    private String assetName;
    private String assetType;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Double speed; // km/h
    private Double heading; // degrees (0-360)
    private Integer satellites; // GPS satellite count
    private Double accuracy; // meters
}