package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.Geofence;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.repository.GeofenceEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class GeofenceEventController {

    private final GeofenceEventRepository geofenceEventRepository;

    /**
     * Creates a new geofence event when an asset enters or exits a geofence
     * This is the core method called by the geofencing engine
     *
     * @param asset The asset that triggered the event
     * @param geofence The geofence that was crossed
     * @param eventType ENTER or EXIT
     * @param lat Current latitude of the asset
     * @param lon Current longitude of the asset
     * @return The created event
     */
    @Transactional
    public GeofenceEvent createEvent(Asset asset, Geofence geofence,
                                     GeofenceEvent.EventType eventType,
                                     double lat, double lon) {
        log.info("Creating {} event for asset {} in geofence {}",
                eventType, asset.getName(), geofence.getName());

        GeofenceEvent event = new GeofenceEvent();
        event.setAsset(asset);
        event.setGeofence(geofence);
        event.setEventType(eventType);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setTimestamp(LocalDateTime.now());
        event.setNotificationSent(false);

        GeofenceEvent savedEvent = geofenceEventRepository.save(event);

        // Future enhancement: Trigger notification here
        // sendNotification(savedEvent);

        return savedEvent;
    }

    /**
     * Get all geofence events in the system
     * Use with caution in production - could return large datasets
     *
     * @return List of all events
     */
    public List<GeofenceEvent> getAllEvents() {
        log.debug("Fetching all geofence events");
        return geofenceEventRepository.findAll();
    }

    /**
     * Get all events for a specific asset
     * Useful for tracking movement history of a vehicle/person
     *
     * Example: "Show me all the places Delivery Van 1 has been"
     *
     * @param assetId The asset ID
     * @return List of events for that asset
     */
    public List<GeofenceEvent> getEventsByAsset(Long assetId) {
        log.debug("Fetching events for asset ID: {}", assetId);
        return geofenceEventRepository.findByAssetId(assetId);
    }

    /**
     * Get all events for a specific geofence
     * Useful for monitoring activity in a particular zone
     *
     * Example: "Show me everyone who entered the warehouse today"
     *
     * @param geofenceId The geofence ID
     * @return List of events in that geofence
     */
    public List<GeofenceEvent> getEventsByGeofence(Long geofenceId) {
        log.debug("Fetching events for geofence ID: {}", geofenceId);
        return geofenceEventRepository.findByGeofenceId(geofenceId);
    }

    /**
     * Get events within a date range
     * Essential for generating reports and analytics
     *
     * Example: "Show me all events from last week"
     *
     * @param start Start date/time
     * @param end End date/time
     * @return List of events in the date range
     */
    public List<GeofenceEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching events between {} and {}", start, end);
        return geofenceEventRepository.findByDateRange(start, end);
    }

    /**
     * Get the last event for an asset in a specific geofence
     * CRITICAL for state tracking!
     *
     * This tells us: "Was this asset last seen entering or exiting?"
     * Used to prevent duplicate events and determine current state
     *
     * Example: If last event was ENTER and asset is still inside = no new event
     *          If last event was ENTER and asset is now outside = trigger EXIT event
     *
     * @param assetId The asset ID
     * @param geofenceId The geofence ID
     * @return Optional containing the last event, or empty if no previous events
     */
    public Optional<GeofenceEvent> getLastEventForAssetInGeofence(Long assetId, Long geofenceId) {
        log.debug("Fetching last event for asset {} in geofence {}", assetId, geofenceId);
        return geofenceEventRepository.findLastEventForAssetInGeofence(assetId, geofenceId);
    }

    /**
     * Count events by type within a date range
     * Used for analytics and dashboards
     *
     * Example: "How many vehicles entered the warehouse this week?"
     *
     * @param eventType ENTER or EXIT
     * @param start Start date/time
     * @param end End date/time
     * @return Count of events
     */
    public Long countEventsByType(GeofenceEvent.EventType eventType,
                                  LocalDateTime start, LocalDateTime end) {
        log.debug("Counting {} events between {} and {}", eventType, start, end);
        return geofenceEventRepository.countByEventTypeAndDateRange(eventType, start, end);
    }

    /**
     * Get the most active assets in a time period
     * Useful for identifying which assets are moving the most
     *
     * Returns array: [assetId, eventCount]
     *
     * Example: "Which delivery vehicle was most active this week?"
     *
     * @param start Start date/time
     * @param end End date/time
     * @return List of [assetId, count] pairs, sorted by count descending
     */
    public List<Object[]> getMostActiveAssets(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching most active assets between {} and {}", start, end);
        return geofenceEventRepository.findMostActiveAssets(start, end);
    }

    /**
     * Check if an asset is currently inside a geofence
     * Based on the last recorded event
     *
     * @param assetId The asset ID
     * @param geofenceId The geofence ID
     * @return true if the last event was ENTER, false otherwise
     */
    public boolean isAssetCurrentlyInGeofence(Long assetId, Long geofenceId) {
        Optional<GeofenceEvent> lastEvent = getLastEventForAssetInGeofence(assetId, geofenceId);
        return lastEvent.isPresent() && lastEvent.get().getEventType() == GeofenceEvent.EventType.ENTER;
    }

    /**
     * Get event statistics for a specific asset
     *
     * @param assetId The asset ID
     * @param start Start date/time
     * @param end End date/time
     * @return Map containing entry count, exit count, and total
     */
    public Map<String, Long> getAssetStatistics(Long assetId, LocalDateTime start, LocalDateTime end) {
        List<GeofenceEvent> events = geofenceEventRepository.findByAssetIdAndDateRange(assetId, start, end);

        long entryCount = events.stream()
                .filter(e -> e.getEventType() == GeofenceEvent.EventType.ENTER)
                .count();
        long exitCount = events.stream()
                .filter(e -> e.getEventType() == GeofenceEvent.EventType.EXIT)
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalEntries", entryCount);
        stats.put("totalExits", exitCount);
        stats.put("totalEvents", (long) events.size());

        return stats;
    }

    /**
     * Mark an event as having notification sent
     * Used by notification services
     *
     * @param eventId The event ID
     */
    @Transactional
    public void markNotificationSent(Long eventId) {
        geofenceEventRepository.findById(eventId).ifPresent(event -> {
            event.setNotificationSent(true);
            geofenceEventRepository.save(event);
            log.info("Marked notification as sent for event ID: {}", eventId);
        });
    }
}
