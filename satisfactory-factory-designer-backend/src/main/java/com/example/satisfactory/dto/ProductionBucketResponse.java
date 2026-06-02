package com.example.satisfactory.dto;

public record ProductionBucketResponse(
        Long id,
        Long factoryId,
        String name,
        boolean enabled,
        String description,
        Double positionX,
        Double positionY,
        boolean collapsed,
        Integer sortOrder
) {}
