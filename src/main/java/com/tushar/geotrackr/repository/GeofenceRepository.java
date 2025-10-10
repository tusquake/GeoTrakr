package com.tushar.geotrackr.repository;

import com.tushar.geotrackr.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
    List<Geofence> findByActiveTrue();
    List<Geofence> findByCreatedById(Long userId);

    @Query("SELECT g FROM Geofence g WHERE g.active = true AND g.type = :type")
    List<Geofence> findActiveGeofencesByType(@Param("type") Geofence.GeofenceType type);
}
