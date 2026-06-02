package com.example.satisfactory.service;

import com.example.satisfactory.dto.FactoryRequest;
import com.example.satisfactory.dto.FactoryResponse;
import com.example.satisfactory.entity.Factory;
import com.example.satisfactory.enums.FactoryType;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.FactoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FactoryService {
    private final FactoryRepository factoryRepository;

    public FactoryService(FactoryRepository factoryRepository) {
        this.factoryRepository = factoryRepository;
    }

    @Transactional(readOnly = true)
    public List<FactoryResponse> findAll() {
        return factoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FactoryResponse findOne(Long id) {
        return toResponse(getFactory(id));
    }

    public FactoryResponse create(FactoryRequest request) {
        Factory factory = new Factory();
        apply(factory, request);
        return toResponse(factoryRepository.save(factory));
    }

    public FactoryResponse update(Long id, FactoryRequest request) {
        Factory factory = getFactory(id);
        apply(factory, request);
        return toResponse(factory);
    }

    public void delete(Long id) {
        if (!factoryRepository.existsById(id)) {
            throw new NotFoundException("Factory not found: " + id);
        }
        factoryRepository.deleteById(id);
    }

    public Factory getFactory(Long id) {
        return factoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Factory not found: " + id));
    }

    private void apply(Factory factory, FactoryRequest request) {
        factory.setName(request.name());
        factory.setFactoryType(request.factoryType() == null ? FactoryType.SUB : request.factoryType());
        if (request.enabled() != null) factory.setEnabled(request.enabled());
        if (request.maxBeltLevel() != null) factory.setMaxBeltLevel(request.maxBeltLevel());
        if (request.maxPipeLevel() != null) factory.setMaxPipeLevel(request.maxPipeLevel());
        factory.setDescription(request.description());
    }

    public FactoryResponse toResponse(Factory factory) {
        return new FactoryResponse(factory.getId(), factory.getName(), factory.getFactoryType(), factory.isEnabled(), factory.getMaxBeltLevel(), factory.getMaxPipeLevel(), factory.getDescription(), factory.getCreatedAt(), factory.getUpdatedAt());
    }
}
