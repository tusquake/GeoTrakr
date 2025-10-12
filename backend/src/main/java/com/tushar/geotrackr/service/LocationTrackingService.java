package com.tushar.geotrackr.service;

import com.tushar.geotrackr.dto.LocationUpdateDTO;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GeofenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for tracking asset locations and triggering geofence checks
 * Now with WebSocket real-time broadcasting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {

    private final AssetService assetService;
    private final GeofenceService geofenceService;
    private final WebSocketService webSocketService;

    /**
     * Process location update from REST API or WebSocket
     * Updates asset location and checks geofences
     * Broadcasts updates to WebSocket subscribers
     */
    @Transactional
    public List<GeofenceEvent> processLocationUpdate(LocationUpdateDTO locationUpdate) {
        log.info("Processing location update for asset {}: ({}, {})",
                locationUpdate.getAssetId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude());

        // Update asset location in database
        Asset asset = assetService.updateAssetLocation(
                locationUpdate.getAssetId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude()
        );

        // Broadcast location update via WebSocket
        webSocketService.broadcastLocationUpdate(
                webSocketService.createLocationMessage(asset)
        );

        // Check all geofences for this location
        List<GeofenceEvent> events = geofenceService.checkGeofences(
                asset,
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude()
        );

        // Broadcast each geofence event via WebSocket
        if (!events.isEmpty()) {
            log.info("Location update triggered {} geofence event(s)", events.size());
            for (GeofenceEvent event : events) {
                webSocketService.broadcastGeofenceEvent(
                        webSocketService.createEventMessage(event)
                );
            }
        }

        return events;
    }

    /**
     * Get current location of an asset
     */
    public Asset getAssetCurrentLocation(Long assetId) {
        return assetService.getAssetById(assetId);
    }

    /**
     * Get current locations of all active assets
     */
    public List<Asset> getAllAssetLocations() {
        return assetService.getActiveAssets();
    }
}