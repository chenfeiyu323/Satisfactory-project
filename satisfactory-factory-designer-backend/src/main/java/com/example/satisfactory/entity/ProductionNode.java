package com.example.satisfactory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "production_nodes")
public class ProductionNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bucket_id", nullable = false)
    private ProductionBucket bucket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "machine_count", nullable = false)
    private Double machineCount = 1.0;

    @Column(name = "clock_percent", nullable = false)
    private Double clockPercent = 100.0;

    @Column(name = "output_multiplier", nullable = false)
    private Double outputMultiplier = 1.0;

    @Column(length = 160)
    private String name;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductionBucket getBucket() { return bucket; }
    public void setBucket(ProductionBucket bucket) { this.bucket = bucket; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Double getMachineCount() { return machineCount; }
    public void setMachineCount(Double machineCount) { this.machineCount = machineCount; }
    public Double getClockPercent() { return clockPercent; }
    public void setClockPercent(Double clockPercent) { this.clockPercent = clockPercent; }
    public Double getOutputMultiplier() { return outputMultiplier; }
    public void setOutputMultiplier(Double outputMultiplier) { this.outputMultiplier = outputMultiplier; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPositionX() { return positionX; }
    public void setPositionX(Double positionX) { this.positionX = positionX; }
    public Double getPositionY() { return positionY; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
