package com.tushar.geotrackr.service;

import com.tushar.geotrackr.dto.EnhancedLocationUpdateDTO;
import com.tushar.geotrackr.dto.GPSStatusMessage;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GPSData;
import com.tushar.geotrackr.repository.AssetRepository;
import com.tushar.geotrackr.repository.GPSDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for processing GPS data with quality metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GPSTrackingService {

    private final GPSDataRepository gpsDataRepository;
    private final AssetRepository assetRepository;
    private final WebSocketService webSocketService;

    /**
     * Process enhanced location update with GPS metadata
     * Validates GPS quality before accepting update
     */
    @Transactional
    public GPSData processGPSUpdate(EnhancedLocationUpdateDTO dto) {
        log.debug("Processing GPS update for asset {}", dto.getAssetId());

        Asset asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + dto.getAssetId()));

        // Validate GPS quality
        if (!isAcceptableGPSQuality(dto)) {
            log.warn("GPS quality too low for asset {}: satellites={}, accuracy={}m",
                    dto.getAssetId(), dto.getSatellites(), dto.getAccuracy());

            // Broadcast GPS status warning
            GPSStatusMessage statusMessage = new GPSStatusMessage(
                    dto.getAssetId(),
                    "WEAK_SIGNAL",
                    dto.getSatellites(),
                    dto.getAccuracy(),
                    LocalDateTime.now()
            );
            webSocketService.broadcastGPSStatus(statusMessage);

            // Still save the data but don't update asset's current location
            return saveGPSData(asset, dto);
        }

        // Update asset's current location
        asset.setCurrentLatitude(dto.getLatitude());
        asset.setCurrentLongitude(dto.getLongitude());
        asset.setLastUpdate(LocalDateTime.now());
        assetRepository.save(asset);

        // Save detailed GPS data
        GPSData gpsData = saveGPSData(asset, dto);

        // Broadcast GPS status
        GPSStatusMessage statusMessage = new GPSStatusMessage(
                dto.getAssetId(),
                "CONNECTED",
                dto.getSatellites(),
                dto.getAccuracy(),
                LocalDateTime.now()
        );
        webSocketService.broadcastGPSStatus(statusMessage);

        return gpsData;
    }

    /**
     * Check if GPS quality is acceptable
     */
    private boolean isAcceptableGPSQuality(EnhancedLocationUpdateDTO dto) {
        // Minimum requirements:
        // - At least 4 satellites
        // - Accuracy better than 50 meters
        // - HDOP less than 5.0
        return (dto.getSatellites() != null && dto.getSatellites() >= 4)
                && (dto.getAccuracy() != null && dto.getAccuracy() <= 50.0)
                && (dto.getHdop() == null || dto.getHdop() < 5.0);
    }

    /**
     * Save GPS data to database
     */
    private GPSData saveGPSData(Asset asset, EnhancedLocationUpdateDTO dto) {
        GPSData gpsData = new GPSData();
        gpsData.setAsset(asset);
        gpsData.setLatitude(dto.getLatitude());
        gpsData.setLongitude(dto.getLongitude());
        gpsData.setAltitude(dto.getAltitude());
        gpsData.setSpeed(dto.getSpeed());
        gpsData.setHeading(dto.getHeading());
        gpsData.setAccuracy(dto.getAccuracy());
        gpsData.setSatellites(dto.getSatellites());
        gpsData.setHdop(dto.getHdop());
        gpsData.setProvider(dto.getProvider());

        if (dto.getFixQuality() != null) {
            gpsData.setFixQuality(GPSData.FixQuality.valueOf(dto.getFixQuality()));
        }

        return gpsDataRepository.save(gpsData);
    }

    /**
     * Get GPS history for an asset
     */
    public List<GPSData> getGPSHistory(Long assetId, LocalDateTime start, LocalDateTime end) {
        return gpsDataRepository.findByAssetIdAndRecordedAtBetween(assetId, start, end);
    }

    /**
     * Calculate average GPS quality for an asset
     */
    public GPSQualityStats getGPSQualityStats(Long assetId, LocalDateTime start, LocalDateTime end) {
        List<GPSData> data = gpsDataRepository.findByAssetIdAndRecordedAtBetween(assetId, start, end);

        if (data.isEmpty()) {
            return new GPSQualityStats(0, 0.0, 0.0, 0.0);
        }

        double avgSatellites = data.stream()
                .filter(d -> d.getSatellites() != null)
                .mapToInt(GPSData::getSatellites)
                .average()
                .orElse(0.0);

        double avgAccuracy = data.stream()
                .filter(d -> d.getAccuracy() != null)
                .mapToDouble(GPSData::getAccuracy)
                .average()
                .orElse(0.0);

        double avgHdop = data.stream()
                .filter(d -> d.getHdop() != null)
                .mapToDouble(GPSData::getHdop)
                .average()
                .orElse(0.0);

        return new GPSQualityStats(data.size(), avgSatellites, avgAccuracy, avgHdop);
    }

    /**
     * Calculate speed from two GPS points
     */
    public double calculateSpeed(GPSData point1, GPSData point2) {
        // Calculate distance in meters
        double distance = calculateDistance(
                point1.getLatitude(), point1.getLongitude(),
                point2.getLatitude(), point2.getLongitude()
        );

        // Calculate time difference in seconds
        long timeDiff = java.time.Duration.between(
                point1.getRecordedAt(),
                point2.getRecordedAt()
        ).getSeconds();

        if (timeDiff == 0) {
            return 0.0;
        }

        // Speed in km/h
        return (distance / timeDiff) * 3.6;
    }

    /**
     * Calculate distance between two points (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth's radius in meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Inner class for GPS quality statistics
     */
    public static class GPSQualityStats {
        public final int totalReadings;
        public final double avgSatellites;
        public final double avgAccuracy;
        public final double avgHdop;

        public GPSQualityStats(int totalReadings, double avgSatellites,
                               double avgAccuracy, double avgHdop) {
            this.totalReadings = totalReadings;
            this.avgSatellites = avgSatellites;
            this.avgAccuracy = avgAccuracy;
            this.avgHdop = avgHdop;
        }

        public String getQualityRating() {
            if (avgSatellites >= 8 && avgAccuracy < 5 && avgHdop < 2) {
                return "EXCELLENT";
            } else if (avgSatellites >= 6 && avgAccuracy < 10 && avgHdop < 3) {
                return "GOOD";
            } else if (avgSatellites >= 4 && avgAccuracy < 20 && avgHdop < 5) {
                return "FAIR";
            } else {
                return "POOR";
            }
        }
    }
}