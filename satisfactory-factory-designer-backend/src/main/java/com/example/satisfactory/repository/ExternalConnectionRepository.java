package com.example.satisfactory.repository;

import com.example.satisfactory.entity.ExternalConnection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExternalConnectionRepository extends JpaRepository<ExternalConnection, Long> {
    boolean existsBySourceBusLineId(Long sourceBusLineId);
    Optional<ExternalConnection> findBySourceBusLineId(Long sourceBusLineId);

    @EntityGraph(attributePaths = {"sourceBusLine", "sourceBusLine.factory", "sourceBusLine.material", "targetBusLine", "targetBusLine.factory", "targetBusLine.material"})
    List<ExternalConnection> findByTargetBusLineIdAndEnabledTrue(Long targetBusLineId);

    @EntityGraph(attributePaths = {"sourceBusLine", "sourceBusLine.factory", "sourceBusLine.material", "targetBusLine", "targetBusLine.factory", "targetBusLine.material"})
    List<ExternalConnection> findByTargetBusLineFactoryIdAndEnabledTrue(Long factoryId);
}
