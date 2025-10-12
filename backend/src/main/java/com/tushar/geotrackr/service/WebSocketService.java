package com.tushar.geotrackr.service;

import com.tushar.geotrackr.dto.*;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GeofenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for broadcasting real-time updates via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast location update to all subscribers
     * Topic: /topic/location/all
     */
    public void broadcastLocationUpdate(LocationUpdateMessage message) {
        log.debug("Broadcasting location update for asset: {}", message.getAssetId());
        messagingTemplate.convertAndSend("/topic/location/all", message);
    }

    /**
     * Send location update to specific asset subscribers
     * Topic: /topic/location/{assetId}
     */
    public void sendLocationUpdateToAsset(Long assetId, LocationUpdateMessage message) {
        log.debug("Sending location update to asset {} subscribers", assetId);
        messagingTemplate.convertAndSend("/topic/location/" + assetId, message);
    }

    /**
     * Broadcast geofence event to all subscribers
     * Topic: /topic/events/all
     */
    public void broadcastGeofenceEvent(GeofenceEventMessage message) {
        log.info("Broadcasting geofence event: {} {} {}",
                message.getAssetName(), message.getEventType(), message.getGeofenceName());
        messagingTemplate.convertAndSend("/topic/events/all", message);
    }

    /**
     * Send geofence event to specific asset subscribers
     * Topic: /topic/events/asset/{assetId}
     */
    public void sendGeofenceEventToAsset(Long assetId, GeofenceEventMessage message) {
        messagingTemplate.convertAndSend("/topic/events/asset/" + assetId, message);
    }

    /**
     * Send geofence event to specific geofence subscribers
     * Topic: /topic/events/geofence/{geofenceId}
     */
    public void sendGeofenceEventToGeofence(Long geofenceId, GeofenceEventMessage message) {
        messagingTemplate.convertAndSend("/topic/events/geofence/" + geofenceId, message);
    }

    /**
     * Broadcast GPS status update
     * Topic: /topic/gps/status
     */
    public void broadcastGPSStatus(GPSStatusMessage message) {
        log.debug("Broadcasting GPS status for asset: {}", message.getAssetId());
        messagingTemplate.convertAndSend("/topic/gps/status", message);
    }

    /**
     * Send notification to specific user
     * Queue: /queue/notifications
     */
    public void sendUserNotification(String username, SystemNotification notification) {
        log.info("Sending notification to user: {}", username);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }

    /**
     * Broadcast system notification to all users
     * Topic: /topic/notifications
     */
    public void broadcastSystemNotification(SystemNotification notification) {
        log.info("Broadcasting system notification: {}", notification.getTitle());
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Helper method to create LocationUpdateMessage from Asset
     */
    public LocationUpdateMessage createLocationMessage(Asset asset) {
        return new LocationUpdateMessage(
                asset.getId(),
                asset.getName(),
                asset.getType().name(),
                asset.getCurrentLatitude(),
                asset.getCurrentLongitude(),
                asset.getLastUpdate(),
                null, // speed - can be calculated
                null, // heading
                null, // satellites
                null  // accuracy
        );
    }

    /**
     * Helper method to create GeofenceEventMessage from GeofenceEvent
     */
    public GeofenceEventMessage createEventMessage(GeofenceEvent event) {
        String message = String.format("%s has %s %s",
                event.getAsset().getName(),
                event.getEventType() == GeofenceEvent.EventType.ENTER ? "entered" : "exited",
                event.getGeofence().getName());

        return new GeofenceEventMessage(
                event.getId(),
                event.getAsset().getId(),
                event.getAsset().getName(),
                event.getGeofence().getId(),
                event.getGeofence().getName(),
                event.getEventType().name(),
                event.getLatitude(),
                event.getLongitude(),
                event.getTimestamp(),
                message
        );
    }
}