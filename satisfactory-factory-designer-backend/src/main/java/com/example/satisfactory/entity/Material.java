package com.example.satisfactory.entity;

import com.example.satisfactory.enums.MaterialType;
import jakarta.persistence.*;

@Entity
@Table(name = "materials", uniqueConstraints = @UniqueConstraint(name = "uk_material_game_key", columnNames = "game_key"))
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_key", nullable = false, length = 120)
    private String gameKey;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 20)
    private MaterialType materialType;

    @Column(name = "stack_size")
    private Integer stackSize;

    @Column(nullable = false)
    private boolean sinkable = true;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) { this.materialType = materialType; }
    public Integer getStackSize() { return stackSize; }
    public void setStackSize(Integer stackSize) { this.stackSize = stackSize; }
    public boolean isSinkable() { return sinkable; }
    public void setSinkable(boolean sinkable) { this.sinkable = sinkable; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
