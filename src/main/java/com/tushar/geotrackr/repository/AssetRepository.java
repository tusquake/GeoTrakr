package com.tushar.geotrackr.repository;

import com.tushar.geotrackr.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByOwnerId(Long ownerId);
    List<Asset> findByActiveTrue();
    List<Asset> findByOwnerIdAndActiveTrue(Long ownerId);

    @Query("SELECT a FROM Asset a WHERE a.owner.id = :ownerId AND a.type = :type")
    List<Asset> findByOwnerIdAndType(@Param("ownerId") Long ownerId,
                                     @Param("type") Asset.AssetType type);
}
