package com.tushar.geotrackr.service;

import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.Geofence;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.repository.GeofenceEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceEventService {

    private final GeofenceEventRepository geofenceEventRepository;

    @Transactional
    public GeofenceEvent createEvent(Asset asset, Geofence geofence,
                                     GeofenceEvent.EventType eventType,
                                     double lat, double lon) {
        GeofenceEvent event = new GeofenceEvent();
        event.setAsset(asset);
        event.setGeofence(geofence);
        event.setEventType(eventType);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setTimestamp(LocalDateTime.now());
        event.setNotificationSent(false);

        return geofenceEventRepository.save(event);
    }

    public List<GeofenceEvent> getAllEvents() {
        return geofenceEventRepository.findAll();
    }

    public List<GeofenceEvent> getEventsByAsset(Long assetId) {
        return geofenceEventRepository.findByAssetId(assetId);
    }

    public List<GeofenceEvent> getEventsByGeofence(Long geofenceId) {
        return geofenceEventRepository.findByGeofenceId(geofenceId);
    }

    public List<GeofenceEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return geofenceEventRepository.findByDateRange(start, end);
    }

    public Optional<GeofenceEvent> getLastEventForAssetInGeofence(Long assetId, Long geofenceId) {
        return geofenceEventRepository.findLastEventForAssetInGeofence(assetId, geofenceId);
    }

    public Long countEventsByType(GeofenceEvent.EventType eventType,
                                  LocalDateTime start, LocalDateTime end) {
        return geofenceEventRepository.countByEventTypeAndDateRange(eventType, start, end);
    }

    public List<Object[]> getMostActiveAssets(LocalDateTime start, LocalDateTime end) {
        return geofenceEventRepository.findMostActiveAssets(start, end);
    }
}
