package com.example.satisfactory.dto;

import java.time.Instant;

public record ExternalConnectionResponse(
        Long id,
        Long sourceBusLineId,
        String sourceFactoryName,
        String sourceLineName,
        Long targetBusLineId,
        String targetFactoryName,
        String targetLineName,
        boolean enabled,
        Instant createdAt
) {}
