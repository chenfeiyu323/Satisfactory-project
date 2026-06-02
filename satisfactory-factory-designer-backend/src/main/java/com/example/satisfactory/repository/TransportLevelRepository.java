package com.example.satisfactory.repository;

import com.example.satisfactory.entity.TransportLevel;
import com.example.satisfactory.enums.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransportLevelRepository extends JpaRepository<TransportLevel, Long> {
    Optional<TransportLevel> findByTransportTypeAndLevel(TransportType type, Integer level);
    List<TransportLevel> findByTransportTypeOrderBySortOrderAsc(TransportType type);
}
