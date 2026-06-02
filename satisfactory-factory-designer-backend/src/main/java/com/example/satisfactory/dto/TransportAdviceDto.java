package com.example.satisfactory.dto;

import com.example.satisfactory.enums.TransportType;

public record TransportAdviceDto(
        TransportType transportType,
        Double requiredThroughput,
        Integer currentMaxLevel,
        Double currentMaxCapacity,
        Integer recommendedLevel,
        String recommendedName,
        String message,
        boolean overCurrentMax
) {}
