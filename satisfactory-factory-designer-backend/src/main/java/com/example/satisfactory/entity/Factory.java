package com.example.satisfactory.entity;

import com.example.satisfactory.enums.FactoryType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "factories")
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "factory_type", nullable = false, length = 20)
    private FactoryType factoryType = FactoryType.SUB;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "max_belt_level", nullable = false)
    private Integer maxBeltLevel = 3;

    @Column(name = "max_pipe_level", nullable = false)
    private Integer maxPipeLevel = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FactoryType getFactoryType() { return factoryType; }
    public void setFactoryType(FactoryType factoryType) { this.factoryType = factoryType; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Integer getMaxBeltLevel() { return maxBeltLevel; }
    public void setMaxBeltLevel(Integer maxBeltLevel) { this.maxBeltLevel = maxBeltLevel; }
    public Integer getMaxPipeLevel() { return maxPipeLevel; }
    public void setMaxPipeLevel(Integer maxPipeLevel) { this.maxPipeLevel = maxPipeLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
