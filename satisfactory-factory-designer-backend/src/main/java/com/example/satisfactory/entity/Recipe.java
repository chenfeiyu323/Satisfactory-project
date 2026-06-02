package com.example.satisfactory.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes", uniqueConstraints = @UniqueConstraint(name = "uk_recipe_game_key", columnNames = "game_key"))
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_key", nullable = false, length = 160)
    private String gameKey;

    @Column(nullable = false, length = 180)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "cycle_time_seconds", nullable = false)
    private Double cycleTimeSeconds;

    @Column(name = "is_alternate", nullable = false)
    private boolean alternate = false;

    @Column(nullable = false, length = 60)
    private String source = "OFFICIAL";

    @Column(name = "game_version", length = 60)
    private String gameVersion;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeInput> inputs = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeOutput> outputs = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }
    public Double getCycleTimeSeconds() { return cycleTimeSeconds; }
    public void setCycleTimeSeconds(Double cycleTimeSeconds) { this.cycleTimeSeconds = cycleTimeSeconds; }
    public boolean isAlternate() { return alternate; }
    public void setAlternate(boolean alternate) { this.alternate = alternate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getGameVersion() { return gameVersion; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<RecipeInput> getInputs() { return inputs; }
    public void setInputs(List<RecipeInput> inputs) { this.inputs = inputs; }
    public List<RecipeOutput> getOutputs() { return outputs; }
    public void setOutputs(List<RecipeOutput> outputs) { this.outputs = outputs; }
}
