package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.repository.FieldMappingRepository;
import com.example.datasynchronizationtool.service.IFieldMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FieldMappingServiceImpl implements IFieldMappingService {
    private final FieldMappingRepository fieldMappingRepository;

    @Autowired
    public FieldMappingServiceImpl(FieldMappingRepository fieldMappingRepository) {
        this.fieldMappingRepository = fieldMappingRepository;
    }

    @Override
    public List<FieldMapping> getAllFieldMappings() {
        return fieldMappingRepository.findAll();
    }

    @Override
    public Optional<FieldMapping> getFieldMappingById(Long id) {
        return fieldMappingRepository.findById(id);
    }

    @Override
    public FieldMapping saveFieldMapping(FieldMapping fieldMapping) {
        return fieldMappingRepository.save(fieldMapping);
    }

    @Override
    public void deleteFieldMapping(Long id) {
        fieldMappingRepository.deleteById(id);
    }
}
