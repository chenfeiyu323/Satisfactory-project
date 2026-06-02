package com.example.satisfactory.dto;

import jakarta.validation.constraints.NotNull;

public record ProductionNodeRequest(
        @NotNull Long recipeId,
        Boolean enabled,
        Double machineCount,
        Double clockPercent,
        Double outputMultiplier,
        String name,
        Double positionX,
        Double positionY,
        Integer sortOrder
) {}
