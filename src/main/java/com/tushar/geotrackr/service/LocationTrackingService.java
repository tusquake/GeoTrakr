package com.tushar.geotrackr.service;

import com.tushar.geotrackr.dto.LocationUpdateDTO;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GeofenceEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {

    private final AssetService assetService;
    private final GeofenceService geofenceService;

    @Transactional
    public List<GeofenceEvent> processLocationUpdate(LocationUpdateDTO locationUpdate) {
        log.info("Processing location update for asset {}: ({}, {})",
                locationUpdate.getAssetId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude());

        // Update asset location
        Asset asset = assetService.updateAssetLocation(
                locationUpdate.getAssetId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude()
        );

        // Check all geofences
        List<GeofenceEvent> events = geofenceService.checkGeofences(
                asset,
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude()
        );

        log.info("Location update processed. {} geofence events triggered", events.size());

        return events;
    }

    public Asset getAssetCurrentLocation(Long assetId) {
        return assetService.getAssetById(assetId);
    }

    public List<Asset> getAllAssetLocations() {
        return assetService.getActiveAssets();
    }
}
