package com.example.satisfactory.service;

import com.example.satisfactory.calculator.FactoryCalculationService;
import com.example.satisfactory.dto.CreateExternalConnectionRequest;
import com.example.satisfactory.dto.ExternalConnectionResponse;
import com.example.satisfactory.dto.ExternalSourceOptionResponse;
import com.example.satisfactory.entity.BusLine;
import com.example.satisfactory.entity.ExternalConnection;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.BusLineRepository;
import com.example.satisfactory.repository.ExternalConnectionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ExternalConnectionService {
    private final ExternalConnectionRepository connectionRepository;
    private final BusLineRepository busLineRepository;
    private final FactoryCalculationService calculationService;

    public ExternalConnectionService(ExternalConnectionRepository connectionRepository, BusLineRepository busLineRepository, @Lazy FactoryCalculationService calculationService) {
        this.connectionRepository = connectionRepository;
        this.busLineRepository = busLineRepository;
        this.calculationService = calculationService;
    }

    @Transactional(readOnly = true)
    public List<ExternalSourceOptionResponse> availableSources(Long targetBusLineId) {
        BusLine target = getBusLine(targetBusLineId);
        return busLineRepository.findByMaterialIdAndVisibleToOtherFactoriesTrueAndExternalEnabledTrue(target.getMaterial().getId()).stream()
                .filter(source -> !Objects.equals(source.getFactory().getId(), target.getFactory().getId()))
                .filter(source -> source.getFactory().isEnabled())
                .filter(source -> !connectionRepository.existsBySourceBusLineId(source.getId()))
                .map(source -> {
                    double amount = Math.max(0.0, calculationService.calculateBusLineNet(source.getId()));
                    return new ExternalSourceOptionResponse(
                            source.getId(),
                            source.getFactory().getId(),
                            source.getFactory().getName(),
                            source.getName(),
                            source.getFactory().getName() + " / " + source.getName(),
                            amount
                    );
                })
                .filter(option -> option.availableAmount() > 0.000001)
                .toList();
    }

    public ExternalConnectionResponse create(CreateExternalConnectionRequest request) {
        BusLine source = getBusLine(request.sourceBusLineId());
        BusLine target = getBusLine(request.targetBusLineId());
        validateConnection(source, target);
        ExternalConnection connection = new ExternalConnection();
        connection.setSourceBusLine(source);
        connection.setTargetBusLine(target);
        connection.setEnabled(true);
        return toResponse(connectionRepository.save(connection));
    }

    public void delete(Long id) {
        if (!connectionRepository.existsById(id)) throw new NotFoundException("External connection not found: " + id);
        connectionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExternalConnectionResponse> findConnectionsIntoFactory(Long factoryId) {
        return connectionRepository.findByTargetBusLineFactoryIdAndEnabledTrue(factoryId).stream().map(this::toResponse).toList();
    }

    private void validateConnection(BusLine source, BusLine target) {
        if (Objects.equals(source.getFactory().getId(), target.getFactory().getId())) {
            throw new BadRequestException("A factory cannot connect to its own external line.");
        }
        if (!source.getFactory().isEnabled()) {
            throw new BadRequestException("Source factory is disabled.");
        }
        if (!Objects.equals(source.getMaterial().getId(), target.getMaterial().getId())) {
            throw new BadRequestException("Source and target material must match.");
        }
        if (!source.isVisibleToOtherFactories() || !source.isExternalEnabled()) {
            throw new BadRequestException("Source bus line is not visible as an external output.");
        }
        if (connectionRepository.existsBySourceBusLineId(source.getId())) {
            throw new BadRequestException("Source bus line is already connected to another target.");
        }
        double sourceNet = calculationService.calculateBusLineNet(source.getId());
        if (sourceNet <= 0.000001) {
            throw new BadRequestException("Source bus line net value must be positive before it can be externalized.");
        }
    }

    private BusLine getBusLine(Long id) {
        return busLineRepository.findById(id).orElseThrow(() -> new NotFoundException("Bus line not found: " + id));
    }

    private ExternalConnectionResponse toResponse(ExternalConnection connection) {
        BusLine source = connection.getSourceBusLine();
        BusLine target = connection.getTargetBusLine();
        return new ExternalConnectionResponse(
                connection.getId(), source.getId(), source.getFactory().getName(), source.getName(), target.getId(), target.getFactory().getName(), target.getName(), connection.isEnabled(), connection.getCreatedAt()
        );
    }
}
