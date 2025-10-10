package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.dto.ApiResponse;
import com.tushar.geotrackr.dto.GeofenceCreateDTO;
import com.tushar.geotrackr.entity.Geofence;
import com.tushar.geotrackr.entity.User;
import com.tushar.geotrackr.repository.UserRepository;
import com.tushar.geotrackr.service.GeofenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geofences")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Geofences", description = "Geofence Management APIs")
public class GeofenceController {

    private final GeofenceService geofenceService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create new geofence")
    public ResponseEntity<ApiResponse<Geofence>> createGeofence(
            @Valid @RequestBody GeofenceCreateDTO dto,
            Authentication authentication) {

        // Get the logged-in user
        String username = authentication.getName();
        User createdBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Create Geofence entity
        Geofence geofence = new Geofence();
        geofence.setName(dto.getName());
        geofence.setDescription(dto.getDescription());
        geofence.setType(dto.getType());
        geofence.setCenterLatitude(dto.getCenterLatitude());
        geofence.setCenterLongitude(dto.getCenterLongitude());
        geofence.setRadius(dto.getRadius());
        geofence.setPolygonCoordinates(dto.getPolygonCoordinates());
        geofence.setAlertType(dto.getAlertType());
        geofence.setActive(true);
        geofence.setCreatedBy(createdBy);

        Geofence saved = geofenceService.createGeofence(geofence);

        return ResponseEntity.ok(new ApiResponse<>(true, "Geofence created successfully", saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing geofence")
    public ResponseEntity<ApiResponse<Geofence>> updateGeofence(
            @PathVariable Long id,
            @Valid @RequestBody GeofenceCreateDTO dto) {

        Geofence geofence = new Geofence();
        geofence.setName(dto.getName());
        geofence.setDescription(dto.getDescription());
        geofence.setType(dto.getType());
        geofence.setCenterLatitude(dto.getCenterLatitude());
        geofence.setCenterLongitude(dto.getCenterLongitude());
        geofence.setRadius(dto.getRadius());
        geofence.setPolygonCoordinates(dto.getPolygonCoordinates());
        geofence.setAlertType(dto.getAlertType());

        Geofence updated = geofenceService.updateGeofence(id, geofence);
        return ResponseEntity.ok(new ApiResponse<>(true, "Geofence updated successfully", updated));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete geofence")
    public ResponseEntity<ApiResponse<Void>> deleteGeofence(@PathVariable Long id) {
        geofenceService.deleteGeofence(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Geofence deleted", null));
    }
}