package com.example.satisfactory.dto;

public record ProductionNodeResponse(
        Long id,
        Long bucketId,
        Long recipeId,
        String recipeName,
        boolean enabled,
        Double machineCount,
        Double clockPercent,
        Double outputMultiplier,
        String name,
        Double positionX,
        Double positionY,
        Integer sortOrder
) {}
