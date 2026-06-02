package com.example.satisfactory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bus_lines", uniqueConstraints = @UniqueConstraint(name = "uk_bus_line_factory_material", columnNames = {"factory_id", "material_id"}))
public class BusLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "offset_amount", nullable = false)
    private Double offsetAmount = 0.0;

    @Column(name = "visible_to_other_factories", nullable = false)
    private boolean visibleToOtherFactories = false;

    @Column(name = "external_enabled", nullable = false)
    private boolean externalEnabled = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean collapsed = false;

    @Column(name = "created_manually", nullable = false)
    private boolean createdManually = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getOffsetAmount() { return offsetAmount; }
    public void setOffsetAmount(Double offsetAmount) { this.offsetAmount = offsetAmount; }
    public boolean isVisibleToOtherFactories() { return visibleToOtherFactories; }
    public void setVisibleToOtherFactories(boolean visibleToOtherFactories) { this.visibleToOtherFactories = visibleToOtherFactories; }
    public boolean isExternalEnabled() { return externalEnabled; }
    public void setExternalEnabled(boolean externalEnabled) { this.externalEnabled = externalEnabled; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isCollapsed() { return collapsed; }
    public void setCollapsed(boolean collapsed) { this.collapsed = collapsed; }
    public boolean isCreatedManually() { return createdManually; }
    public void setCreatedManually(boolean createdManually) { this.createdManually = createdManually; }
}
