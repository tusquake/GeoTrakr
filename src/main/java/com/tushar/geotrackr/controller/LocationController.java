package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.dto.ApiResponse;
import com.tushar.geotrackr.dto.LocationUpdateDTO;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.service.LocationTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Location Tracking", description = "Real-time Location Tracking APIs")
public class LocationController {

    private final LocationTrackingService locationTrackingService;

    @PostMapping("/update")
    @Operation(summary = "Update asset location and check geofences")
    public ResponseEntity<ApiResponse<List<GeofenceEvent>>> updateLocation(
            @Valid @RequestBody LocationUpdateDTO dto) {

        List<GeofenceEvent> events = locationTrackingService.processLocationUpdate(dto);

        String message = events.isEmpty()
                ? "Location updated, no geofence events"
                : String.format("Location updated, %d geofence event(s) triggered", events.size());

        return ResponseEntity.ok(new ApiResponse<>(true, message, events));
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get current location of an asset")
    public ResponseEntity<ApiResponse<Asset>> getAssetLocation(@PathVariable Long assetId) {
        Asset asset = locationTrackingService.getAssetCurrentLocation(assetId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset location retrieved", asset));
    }

    @GetMapping("/all")
    @Operation(summary = "Get current locations of all assets")
    public ResponseEntity<ApiResponse<List<Asset>>> getAllAssetLocations() {
        List<Asset> assets = locationTrackingService.getAllAssetLocations();
        return ResponseEntity.ok(new ApiResponse<>(true, "All asset locations retrieved", assets));
    }
}