package com.tushar.geotrackr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPSStatusMessage {
    private Long assetId;
    private String status; // CONNECTED, DISCONNECTED, WEAK_SIGNAL
    private Integer satellites;
    private Double accuracy;
    private LocalDateTime timestamp;
}
