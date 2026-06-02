package com.example.satisfactory.dto;

import jakarta.validation.constraints.NotNull;

public record BusLineRequest(
        @NotNull Long materialId,
        String name,
        String description,
        Double offsetAmount,
        Boolean visibleToOtherFactories,
        Boolean externalEnabled,
        Integer sortOrder,
        Boolean collapsed,
        Boolean createdManually
) {}
