package com.tushar.geotrackr.dto;

import com.tushar.geotrackr.entity.Asset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetCreateDTO {
    @NotBlank(message = "Asset name is required")
    private String name;

    @NotNull(message = "Asset type is required")
    private Asset.AssetType type;

    private String description;
}
