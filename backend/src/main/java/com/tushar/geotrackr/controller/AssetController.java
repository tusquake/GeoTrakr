package com.tushar.geotrackr.controller;

import com.tushar.geotrackr.dto.ApiResponse;
import com.tushar.geotrackr.dto.AssetCreateDTO;
import com.tushar.geotrackr.entity.Asset;
import com.tushar.geotrackr.entity.User;
import com.tushar.geotrackr.repository.UserRepository;
import com.tushar.geotrackr.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Assets", description = "Asset Management APIs")
public class AssetController {

    private final AssetService assetService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new asset")
    public ResponseEntity<ApiResponse<Asset>> createAsset(
            @Valid @RequestBody AssetCreateDTO dto,
            Authentication authentication) {

        User owner = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setDescription(dto.getDescription());

        Asset created = assetService.createAsset(asset, owner);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset created successfully", created));
    }

    @GetMapping
    @Operation(summary = "Get all assets for current user")
    public ResponseEntity<ApiResponse<List<Asset>>> getAssets(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Asset> assets = assetService.getAssetsByOwner(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Assets retrieved", assets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<ApiResponse<Asset>> getAssetById(@PathVariable Long id) {
        Asset asset = assetService.getAssetById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset found", asset));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset")
    public ResponseEntity<ApiResponse<Asset>> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetCreateDTO dto) {

        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setDescription(dto.getDescription());

        Asset updated = assetService.updateAsset(id, asset);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset updated", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete asset")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset deleted", null));
    }
}