package com.example.satisfactory.dto;

public record ExternalSourceOptionResponse(
        Long sourceBusLineId,
        Long sourceFactoryId,
        String sourceFactoryName,
        String sourceLineName,
        String displayName,
        Double availableAmount
) {}
