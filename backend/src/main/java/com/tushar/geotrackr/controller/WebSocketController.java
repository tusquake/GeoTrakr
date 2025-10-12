package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.dto.GPSStatusMessage;
import com.tushar.geotrackr.dto.LocationUpdateDTO;
import com.tushar.geotrackr.dto.LocationUpdateMessage;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.service.AssetService;
import com.tushar.geotrackr.service.LocationTrackingService;
import com.tushar.geotrackr.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket controller for handling incoming messages from clients
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final LocationTrackingService locationTrackingService;
    private final WebSocketService webSocketService;
    private final AssetService assetService;

    /**
     * Handle location update from GPS device/client
     * Clients send to: /app/location/update
     * Broadcast to: /topic/location/all
     *
     * @param locationUpdate Location data from client
     * @param principal Authenticated user
     * @return LocationUpdateMessage to broadcast
     */
    @MessageMapping("/location/update")
    @SendTo("/topic/location/all")
    public LocationUpdateMessage handleLocationUpdate(
            @Payload LocationUpdateDTO locationUpdate,
            Principal principal) {

        log.info("Received WebSocket location update for asset: {} from user: {}",
                locationUpdate.getAssetId(),
                principal != null ? principal.getName() : "anonymous");

        try {
            // Process location update and check geofences
            List<GeofenceEvent> events = locationTrackingService.processLocationUpdate(locationUpdate);

            // Get updated asset
            Asset asset = assetService.getAssetById(locationUpdate.getAssetId());

            // Create location message
            LocationUpdateMessage message = webSocketService.createLocationMessage(asset);

            // Broadcast geofence events if any
            if (!events.isEmpty()) {
                for (GeofenceEvent event : events) {
                    webSocketService.broadcastGeofenceEvent(
                            webSocketService.createEventMessage(event));
                }
            }

            return message;

        } catch (Exception e) {
            log.error("Error processing WebSocket location update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process location update");
        }
    }

    /**
     * Handle GPS status update
     * Clients send to: /app/gps/status
     * Broadcast to: /topic/gps/status
     */
    @MessageMapping("/gps/status")
    @SendTo("/topic/gps/status")
    public GPSStatusMessage handleGPSStatus(@Payload GPSStatusMessage status) {
        log.debug("Received GPS status for asset: {} - Status: {}",
                status.getAssetId(), status.getStatus());

        status.setTimestamp(LocalDateTime.now());
        return status;
    }

    /**
     * Handle client connection
     * Clients send to: /app/connect
     */
    @MessageMapping("/connect")
    public void handleConnect(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        String username = principal != null ? principal.getName() : "anonymous";

        log.info("WebSocket client connected - Session: {}, User: {}", sessionId, username);

        // Store session attributes if needed
        headerAccessor.getSessionAttributes().put("username", username);
    }

    /**
     * Handle client disconnection
     * Clients send to: /app/disconnect
     */
    @MessageMapping("/disconnect")
    public void handleDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        log.info("WebSocket client disconnected - Session: {}, User: {}", sessionId, username);
    }
}