package com.tushar.geotrackr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceEventMessage {
    private Long eventId;
    private Long assetId;
    private String assetName;
    private Long geofenceId;
    private String geofenceName;
    private String eventType; // ENTER or EXIT
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String message; // Human-readable message
}
