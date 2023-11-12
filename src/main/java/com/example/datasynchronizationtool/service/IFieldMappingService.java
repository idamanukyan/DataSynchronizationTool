package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.model.FieldMapping;

import java.util.List;
import java.util.Optional;

public interface IFieldMappingService {

    List<FieldMapping> getAllFieldMappings();

    Optional<FieldMapping> getFieldMappingById(Long id);

    FieldMapping saveFieldMapping(FieldMapping fieldMapping);

    void deleteFieldMapping(Long id);
}
