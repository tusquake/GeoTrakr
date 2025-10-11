package com.tushar.geotrackr.dto;

import com.tushar.geotrackr.entity.Geofence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceCreateDTO {
    @NotBlank(message = "Geofence name is required")
    private String name;

    private String description;

    @NotNull(message = "Geofence type is required")
    private Geofence.GeofenceType type;

    private Double centerLatitude;
    private Double centerLongitude;
    private Double radius;

    private String polygonCoordinates;

    @NotNull(message = "Alert type is required")
    private Geofence.AlertType alertType;
}
