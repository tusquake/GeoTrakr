package com.tushar.geotrackr.repository;

import com.tushar.geotrackr.entity.GPSData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GPSDataRepository extends JpaRepository<GPSData, Long> {

    /**
     * Find GPS data for an asset within a date range
     */
    List<GPSData> findByAssetIdAndRecordedAtBetween(
            Long assetId,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Find latest GPS data for an asset
     */
    @Query("SELECT g FROM GPSData g WHERE g.asset.id = :assetId " +
            "ORDER BY g.recordedAt DESC LIMIT 1")
    GPSData findLatestByAssetId(@Param("assetId") Long assetId);

    /**
     * Find GPS data with poor quality (for troubleshooting)
     */
    @Query("SELECT g FROM GPSData g WHERE g.satellites < 4 " +
            "OR g.accuracy > 50 OR g.hdop > 5 " +
            "ORDER BY g.recordedAt DESC")
    List<GPSData> findPoorQualityReadings();

    /**
     * Get average GPS quality metrics for an asset
     */
    @Query("SELECT AVG(g.satellites), AVG(g.accuracy), AVG(g.hdop) " +
            "FROM GPSData g WHERE g.asset.id = :assetId " +
            "AND g.recordedAt BETWEEN :start AND :end")
    Object[] getAverageQualityMetrics(
            @Param("assetId") Long assetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Delete old GPS data (for cleanup)
     */
    void deleteByRecordedAtBefore(LocalDateTime cutoffDate);
}