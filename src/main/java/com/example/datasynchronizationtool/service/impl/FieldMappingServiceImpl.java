package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.exception.ResourceNotFoundException;
import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.repository.FieldMappingRepository;
import com.example.datasynchronizationtool.service.IFieldMappingService;
import com.example.datasynchronizationtool.service.dtos.FieldMappingDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldMappingServiceImpl implements IFieldMappingService {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingServiceImpl.class);

    private final FieldMappingRepository fieldMappingRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FieldMappingServiceImpl(FieldMappingRepository fieldMappingRepository, ModelMapper modelMapper) {
        this.fieldMappingRepository = fieldMappingRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldMappingDto> getAllFieldMappings() {
        log.debug("Fetching all field mappings");
        List<FieldMappingDto> mappings = fieldMappingRepository.findAll().stream()
                .map(fieldMapping -> modelMapper.map(fieldMapping, FieldMappingDto.class))
                .collect(Collectors.toList());
        log.info("Retrieved {} field mappings", mappings.size());
        return mappings;
    }

    @Override
    @Transactional(readOnly = true)
    public FieldMappingDto getFieldMappingById(Long id) {
        log.debug("Fetching field mapping with id: {}", id);
        return fieldMappingRepository.findById(id)
                .map(fieldMapping -> {
                    log.info("Found field mapping: {} -> {} (id: {})",
                            fieldMapping.getSourceField(), fieldMapping.getTargetField(), id);
                    return modelMapper.map(fieldMapping, FieldMappingDto.class);
                })
                .orElseThrow(() -> {
                    log.warn("Field mapping not found with id: {}", id);
                    return new ResourceNotFoundException("FieldMapping", "id", id);
                });
    }

    @Override
    @Transactional
    public FieldMappingDto saveFieldMapping(FieldMappingDto fieldMappingDto) {
        log.debug("Saving field mapping: {} -> {}",
                fieldMappingDto.getSourceField(), fieldMappingDto.getTargetField());
        FieldMapping fieldMapping = modelMapper.map(fieldMappingDto, FieldMapping.class);
        FieldMapping saved = fieldMappingRepository.save(fieldMapping);
        log.info("Saved field mapping: {} -> {} (id: {})",
                saved.getSourceField(), saved.getTargetField(), saved.getId());
        return modelMapper.map(saved, FieldMappingDto.class);
    }

    @Override
    @Transactional
    public void deleteFieldMapping(Long id) {
        log.debug("Deleting field mapping with id: {}", id);
        if (!fieldMappingRepository.existsById(id)) {
            log.warn("Cannot delete - field mapping not found with id: {}", id);
            throw new ResourceNotFoundException("FieldMapping", "id", id);
        }
        fieldMappingRepository.deleteById(id);
        log.info("Deleted field mapping with id: {}", id);
    }
}
