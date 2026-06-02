package com.example.satisfactory.dto;

import jakarta.validation.constraints.NotNull;

public record CreateExternalConnectionRequest(
        @NotNull Long sourceBusLineId,
        @NotNull Long targetBusLineId
) {}
