package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.service.dtos.FieldMappingDto;

import java.util.List;

public interface IFieldMappingService {

    List<FieldMappingDto> getAllFieldMappings();

    FieldMappingDto getFieldMappingById(Long id);

    FieldMappingDto saveFieldMapping(FieldMappingDto fieldMappingDto);

    void deleteFieldMapping(Long id);
}
