package com.example.satisfactory.repository;

import com.example.satisfactory.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByGameKey(String gameKey);
}
