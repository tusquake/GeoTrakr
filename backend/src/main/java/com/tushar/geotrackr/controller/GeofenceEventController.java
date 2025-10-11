package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.dto.ApiResponse;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.service.GeofenceEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Geofence Event operations
 * Provides endpoints for retrieving event history and analytics
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Geofence Events", description = "Geofence Event History and Analytics APIs")
public class GeofenceEventController {

    private final GeofenceEventService geofenceEventService;

    /**
     * Get all geofence events
     * @return List of all events
     */
    @GetMapping
    @Operation(summary = "Get all geofence events", description = "Retrieves complete event history")
    public ResponseEntity<ApiResponse<List<GeofenceEvent>>> getAllEvents() {
        List<GeofenceEvent> events = geofenceEventService.getAllEvents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Events retrieved successfully", events));
    }

    /**
     * Get events for a specific asset
     * @param assetId ID of the asset
     * @return List of events for the specified asset
     */
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get events by asset", description = "Retrieves all events for a specific asset")
    public ResponseEntity<ApiResponse<List<GeofenceEvent>>> getEventsByAsset(
            @PathVariable Long assetId) {
        List<GeofenceEvent> events = geofenceEventService.getEventsByAsset(assetId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset events retrieved successfully", events));
    }

    /**
     * Get events for a specific geofence
     * @param geofenceId ID of the geofence
     * @return List of events in the specified geofence
     */
    @GetMapping("/geofence/{geofenceId}")
    @Operation(summary = "Get events by geofence", description = "Retrieves all events for a specific geofence")
    public ResponseEntity<ApiResponse<List<GeofenceEvent>>> getEventsByGeofence(
            @PathVariable Long geofenceId) {
        List<GeofenceEvent> events = geofenceEventService.getEventsByGeofence(geofenceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Geofence events retrieved successfully", events));
    }

    /**
     * Get events within a date range
     * @param start Start date/time (ISO format: 2025-10-01T00:00:00)
     * @param end End date/time (ISO format: 2025-10-31T23:59:59)
     * @return List of events within the date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get events by date range", description = "Retrieves events between specified start and end dates")
    public ResponseEntity<ApiResponse<List<GeofenceEvent>>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<GeofenceEvent> events = geofenceEventService.getEventsByDateRange(start, end);
        return ResponseEntity.ok(new ApiResponse<>(true, "Events retrieved successfully", events));
    }

    /**
     * Get event statistics for a date range
     * @param start Start date/time
     * @param end End date/time
     * @return Statistics including total entries, exits, and most active assets
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get event statistics", description = "Retrieves aggregated statistics for events in a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Long entryCount = geofenceEventService.countEventsByType(
                GeofenceEvent.EventType.ENTER, start, end);
        Long exitCount = geofenceEventService.countEventsByType(
                GeofenceEvent.EventType.EXIT, start, end);
        List<Object[]> mostActive = geofenceEventService.getMostActiveAssets(start, end);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEntries", entryCount);
        stats.put("totalExits", exitCount);
        stats.put("totalEvents", entryCount + exitCount);
        stats.put("mostActiveAssets", mostActive);

        return ResponseEntity.ok(new ApiResponse<>(true, "Statistics retrieved successfully", stats));
    }
}