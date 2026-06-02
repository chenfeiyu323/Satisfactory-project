package com.example.satisfactory.entity;

import com.example.satisfactory.enums.MachineType;
import jakarta.persistence.*;

@Entity
@Table(name = "machines", uniqueConstraints = @UniqueConstraint(name = "uk_machine_game_key", columnNames = "game_key"))
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_key", nullable = false, length = 120)
    private String gameKey;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "machine_type", nullable = false, length = 40)
    private MachineType machineType;

    @Column(name = "power_mw")
    private Double powerMw;

    @Column(nullable = false)
    private boolean enabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MachineType getMachineType() { return machineType; }
    public void setMachineType(MachineType machineType) { this.machineType = machineType; }
    public Double getPowerMw() { return powerMw; }
    public void setPowerMw(Double powerMw) { this.powerMw = powerMw; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
