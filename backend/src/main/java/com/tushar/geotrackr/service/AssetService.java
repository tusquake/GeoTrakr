package com.tushar.geotrackr.service;

import com.tushar.geotrackr.dto.LocationUpdateDTO;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.Geofence;
import com.tushar.geotrackr.entity.GeofenceEvent;
import com.tushar.geotrackr.entity.User;
import com.tushar.geotrackr.repository.AssetRepository;
import com.tushar.geotrackr.repository.GeofenceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public Asset createAsset(Asset asset, User owner) {
        asset.setOwner(owner);
        asset.setActive(true);
        return assetRepository.save(asset);
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    public List<Asset> getActiveAssets() {
        return assetRepository.findByActiveTrue();
    }

    public List<Asset> getAssetsByOwner(Long ownerId) {
        return assetRepository.findByOwnerId(ownerId);
    }

    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
    }

    @Transactional
    public Asset updateAsset(Long id, Asset updatedAsset) {
        Asset existing = getAssetById(id);

        existing.setName(updatedAsset.getName());
        existing.setType(updatedAsset.getType());
        existing.setDescription(updatedAsset.getDescription());
        existing.setActive(updatedAsset.isActive());

        return assetRepository.save(existing);
    }

    @Transactional
    public Asset updateAssetLocation(Long id, double latitude, double longitude) {
        Asset asset = getAssetById(id);
        asset.setCurrentLatitude(latitude);
        asset.setCurrentLongitude(longitude);
        asset.setLastUpdate(LocalDateTime.now());
        return assetRepository.save(asset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        asset.setActive(false);
        assetRepository.save(asset);
    }
}

