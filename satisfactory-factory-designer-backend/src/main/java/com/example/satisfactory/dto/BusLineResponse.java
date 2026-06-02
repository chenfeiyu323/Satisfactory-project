package com.example.satisfactory.dto;

import com.example.satisfactory.enums.MaterialType;

public record BusLineResponse(
        Long id,
        Long factoryId,
        Long materialId,
        String materialName,
        MaterialType materialType,
        String name,
        String description,
        Double offsetAmount,
        boolean visibleToOtherFactories,
        boolean externalEnabled,
        Integer sortOrder,
        boolean collapsed,
        boolean createdManually
) {}
