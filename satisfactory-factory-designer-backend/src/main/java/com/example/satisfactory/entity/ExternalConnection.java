package com.example.satisfactory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "external_connections", uniqueConstraints = @UniqueConstraint(name = "uk_external_source_bus_line", columnNames = "source_bus_line_id"))
public class ExternalConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_bus_line_id", nullable = false)
    private BusLine sourceBusLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_bus_line_id", nullable = false)
    private BusLine targetBusLine;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusLine getSourceBusLine() { return sourceBusLine; }
    public void setSourceBusLine(BusLine sourceBusLine) { this.sourceBusLine = sourceBusLine; }
    public BusLine getTargetBusLine() { return targetBusLine; }
    public void setTargetBusLine(BusLine targetBusLine) { this.targetBusLine = targetBusLine; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
