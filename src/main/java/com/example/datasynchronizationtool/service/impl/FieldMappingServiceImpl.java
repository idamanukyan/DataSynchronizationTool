package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.repository.FieldMappingRepository;
import com.example.datasynchronizationtool.service.IFieldMappingService;
import com.example.datasynchronizationtool.service.dtos.FieldMappingDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldMappingServiceImpl implements IFieldMappingService {

    private final FieldMappingRepository fieldMappingRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FieldMappingServiceImpl(FieldMappingRepository fieldMappingRepository, ModelMapper modelMapper) {
        this.fieldMappingRepository = fieldMappingRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<FieldMappingDto> getAllFieldMappings() {
        return fieldMappingRepository.findAll().stream()
                .map(fieldMapping -> modelMapper.map(fieldMapping, FieldMappingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public FieldMappingDto getFieldMappingById(Long id) {
        return fieldMappingRepository.findById(id)
                .map(fieldMapping -> modelMapper.map(fieldMapping, FieldMappingDto.class))
                .orElse(null);
    }

    @Override
    public FieldMappingDto saveFieldMapping(FieldMappingDto fieldMappingDto) {
        FieldMapping fieldMapping = modelMapper.map(fieldMappingDto, FieldMapping.class);
        FieldMapping saved = fieldMappingRepository.save(fieldMapping);
        return modelMapper.map(saved, FieldMappingDto.class);
    }

    @Override
    public void deleteFieldMapping(Long id) {
        fieldMappingRepository.deleteById(id);
    }
}
