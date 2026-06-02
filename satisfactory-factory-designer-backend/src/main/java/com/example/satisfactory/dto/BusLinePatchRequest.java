package com.example.satisfactory.dto;

public record BusLinePatchRequest(
        String name,
        String description,
        Double offsetAmount,
        Boolean visibleToOtherFactories,
        Boolean externalEnabled,
        Integer sortOrder,
        Boolean collapsed
) {}
