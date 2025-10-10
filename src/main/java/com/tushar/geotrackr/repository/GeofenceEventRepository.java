package com.tushar.geotrackr.repository;

import com.tushar.geotrackr.entity.GeofenceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeofenceEventRepository extends JpaRepository<GeofenceEvent, Long> {
    List<GeofenceEvent> findByAssetId(Long assetId);
    List<GeofenceEvent> findByGeofenceId(Long geofenceId);

    @Query("SELECT e FROM GeofenceEvent e WHERE e.asset.id = :assetId " +
            "AND e.timestamp BETWEEN :startDate AND :endDate ORDER BY e.timestamp DESC")
    List<GeofenceEvent> findByAssetIdAndDateRange(
            @Param("assetId") Long assetId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM GeofenceEvent e WHERE e.geofence.id = :geofenceId " +
            "AND e.timestamp BETWEEN :startDate AND :endDate ORDER BY e.timestamp DESC")
    List<GeofenceEvent> findByGeofenceIdAndDateRange(
            @Param("geofenceId") Long geofenceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM GeofenceEvent e WHERE e.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY e.timestamp DESC")
    List<GeofenceEvent> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find the last event for an asset in a specific geofence
    @Query("SELECT e FROM GeofenceEvent e WHERE e.asset.id = :assetId " +
            "AND e.geofence.id = :geofenceId ORDER BY e.timestamp DESC LIMIT 1")
    Optional<GeofenceEvent> findLastEventForAssetInGeofence(
            @Param("assetId") Long assetId,
            @Param("geofenceId") Long geofenceId
    );

    // Statistics queries
    @Query("SELECT COUNT(e) FROM GeofenceEvent e WHERE e.eventType = :eventType " +
            "AND e.timestamp BETWEEN :startDate AND :endDate")
    Long countByEventTypeAndDateRange(
            @Param("eventType") GeofenceEvent.EventType eventType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e.asset.id, COUNT(e) FROM GeofenceEvent e " +
            "WHERE e.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY e.asset.id ORDER BY COUNT(e) DESC")
    List<Object[]> findMostActiveAssets(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
