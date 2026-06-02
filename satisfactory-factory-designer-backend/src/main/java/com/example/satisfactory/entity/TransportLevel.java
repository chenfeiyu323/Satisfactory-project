package com.example.satisfactory.entity;

import com.example.satisfactory.enums.TransportType;
import jakarta.persistence.*;

@Entity
@Table(name = "transport_levels", uniqueConstraints = @UniqueConstraint(name = "uk_transport_type_level", columnNames = {"transport_type", "level"}))
public class TransportLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 20)
    private TransportType transportType;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "capacity_per_min", nullable = false)
    private Double capacityPerMin;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TransportType getTransportType() { return transportType; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getCapacityPerMin() { return capacityPerMin; }
    public void setCapacityPerMin(Double capacityPerMin) { this.capacityPerMin = capacityPerMin; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
