package com.tushar.geotrackr.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.Geofence;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.repository.GeofenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final GeofenceEventService geofenceEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance between two points using Haversine formula
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c * 1000; // Return distance in meters
    }

    /**
     * Check if a point is inside a circular geofence
     */
    public boolean isInsideCircularGeofence(Geofence geofence, double lat, double lon) {
        if (geofence.getCenterLatitude() == null || geofence.getCenterLongitude() == null
                || geofence.getRadius() == null) {
            return false;
        }

        double distance = calculateDistance(
                geofence.getCenterLatitude(),
                geofence.getCenterLongitude(),
                lat,
                lon
        );

        return distance <= geofence.getRadius();
    }

    /**
     * Check if a point is inside a polygonal geofence using JTS
     */
    public boolean isInsidePolygonalGeofence(Geofence geofence, double lat, double lon) {
        try {
            if (geofence.getPolygonCoordinates() == null) {
                return false;
            }

            List<List<Double>> coordinates = objectMapper.readValue(
                    geofence.getPolygonCoordinates(),
                    new TypeReference<List<List<Double>>>() {}
            );

            if (coordinates.size() < 3) {
                log.warn("Polygon must have at least 3 points");
                return false;
            }

            // Create polygon coordinates
            Coordinate[] coords = new Coordinate[coordinates.size() + 1];
            for (int i = 0; i < coordinates.size(); i++) {
                coords[i] = new Coordinate(coordinates.get(i).get(1), coordinates.get(i).get(0));
            }
            // Close the polygon
            coords[coordinates.size()] = coords[0];

            Polygon polygon = geometryFactory.createPolygon(coords);
            Point point = geometryFactory.createPoint(new Coordinate(lon, lat));

            return polygon.contains(point);

        } catch (Exception e) {
            log.error("Error checking polygonal geofence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if asset is inside a geofence
     */
    public boolean isInsideGeofence(Geofence geofence, double lat, double lon) {
        if (geofence.getType() == Geofence.GeofenceType.CIRCULAR) {
            return isInsideCircularGeofence(geofence, lat, lon);
        } else {
            return isInsidePolygonalGeofence(geofence, lat, lon);
        }
    }

    /**
     * Check all active geofences for an asset's location
     */
    @Transactional
    public List<GeofenceEvent> checkGeofences(Asset asset, double lat, double lon) {
        List<Geofence> activeGeofences = geofenceRepository.findByActiveTrue();
        List<GeofenceEvent> events = new ArrayList<>();

        log.debug("Checking {} active geofences for asset {}", activeGeofences.size(), asset.getId());

        for (Geofence geofence : activeGeofences) {
            boolean currentlyInside = isInsideGeofence(geofence, lat, lon);
            GeofenceEvent.EventType eventType = determineEventType(asset, geofence, currentlyInside);

            if (eventType != null && shouldTriggerAlert(geofence, eventType)) {
                GeofenceEvent event = geofenceEventService.createEvent(
                        asset, geofence, eventType, lat, lon
                );
                events.add(event);
                log.info("Geofence event created: Asset {} {} geofence {}",
                        asset.getId(), eventType, geofence.getName());
            }
        }

        return events;
    }

    /**
     * Determine if this is an ENTER or EXIT event
     */
    private GeofenceEvent.EventType determineEventType(Asset asset, Geofence geofence,
                                                       boolean currentlyInside) {
        // Get last event for this asset in this geofence
        var lastEvent = geofenceEventService.getLastEventForAssetInGeofence(
                asset.getId(), geofence.getId()
        );

        if (lastEvent.isEmpty()) {
            // First time checking, only trigger if currently inside
            return currentlyInside ? GeofenceEvent.EventType.ENTER : null;
        }

        GeofenceEvent.EventType lastEventType = lastEvent.get().getEventType();

        // Check for state change
        if (currentlyInside && lastEventType == GeofenceEvent.EventType.EXIT) {
            return GeofenceEvent.EventType.ENTER;
        } else if (!currentlyInside && lastEventType == GeofenceEvent.EventType.ENTER) {
            return GeofenceEvent.EventType.EXIT;
        }

        return null; // No state change
    }

    /**
     * Check if alert should be triggered based on geofence configuration
     */
    private boolean shouldTriggerAlert(Geofence geofence, GeofenceEvent.EventType eventType) {
        return geofence.getAlertType() == Geofence.AlertType.BOTH ||
                (geofence.getAlertType() == Geofence.AlertType.ENTRY &&
                        eventType == GeofenceEvent.EventType.ENTER) ||
                (geofence.getAlertType() == Geofence.AlertType.EXIT &&
                        eventType == GeofenceEvent.EventType.EXIT);
    }

    // CRUD Operations

    @Transactional
    public Geofence createGeofence(Geofence geofence) {
        validateGeofence(geofence);
        return geofenceRepository.save(geofence);
    }

    public List<Geofence> getAllGeofences() {
        return geofenceRepository.findAll();
    }

    public List<Geofence> getActiveGeofences() {
        return geofenceRepository.findByActiveTrue();
    }

    public Geofence getGeofenceById(Long id) {
        return geofenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Geofence not found with id: " + id));
    }

    @Transactional
    public Geofence updateGeofence(Long id, Geofence updatedGeofence) {
        Geofence existing = getGeofenceById(id);

        existing.setName(updatedGeofence.getName());
        existing.setDescription(updatedGeofence.getDescription());
        existing.setType(updatedGeofence.getType());
        existing.setCenterLatitude(updatedGeofence.getCenterLatitude());
        existing.setCenterLongitude(updatedGeofence.getCenterLongitude());
        existing.setRadius(updatedGeofence.getRadius());
        existing.setPolygonCoordinates(updatedGeofence.getPolygonCoordinates());
        existing.setAlertType(updatedGeofence.getAlertType());
        existing.setActive(updatedGeofence.isActive());

        validateGeofence(existing);
        return geofenceRepository.save(existing);
    }

    @Transactional
    public void deleteGeofence(Long id) {
        Geofence geofence = getGeofenceById(id);
        geofence.setActive(false);
        geofenceRepository.save(geofence);
    }

    private void validateGeofence(Geofence geofence) {
        if (geofence.getType() == Geofence.GeofenceType.CIRCULAR) {
            if (geofence.getCenterLatitude() == null || geofence.getCenterLongitude() == null
                    || geofence.getRadius() == null) {
                throw new IllegalArgumentException(
                        "Circular geofence requires centerLatitude, centerLongitude, and radius");
            }
            if (geofence.getRadius() <= 0) {
                throw new IllegalArgumentException("Radius must be greater than 0");
            }
        } else if (geofence.getType() == Geofence.GeofenceType.POLYGONAL) {
            if (geofence.getPolygonCoordinates() == null) {
                throw new IllegalArgumentException("Polygonal geofence requires coordinates");
            }
        }
    }
}