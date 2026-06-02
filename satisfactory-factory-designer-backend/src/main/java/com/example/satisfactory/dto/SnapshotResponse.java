package com.example.satisfactory.dto;

import java.time.Instant;

public record SnapshotResponse(Long id, Long factoryId, String name, String snapshotJson, Instant createdAt) {}
