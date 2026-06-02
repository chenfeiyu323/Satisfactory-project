package com.example.satisfactory.repository;

import com.example.satisfactory.entity.BusLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BusLineRepository extends JpaRepository<BusLine, Long> {
    Optional<BusLine> findByFactoryIdAndMaterialId(Long factoryId, Long materialId);
    List<BusLine> findByFactoryIdOrderBySortOrderAscIdAsc(Long factoryId);

    @EntityGraph(attributePaths = {"factory", "material"})
    List<BusLine> findByMaterialIdAndVisibleToOtherFactoriesTrueAndExternalEnabledTrue(Long materialId);
}
