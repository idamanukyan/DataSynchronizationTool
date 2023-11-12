package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.IFieldMappingService;
import com.example.datasynchronizationtool.service.dtos.FieldMappingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field-mappings")
public class FieldMappingController {

    private final IFieldMappingService fieldMappingService;

    @Autowired
    public FieldMappingController(IFieldMappingService fieldMappingService) {
        this.fieldMappingService = fieldMappingService;
    }

    @GetMapping
    public List<FieldMappingDto> getAllFieldMappings() {
        return fieldMappingService.getAllFieldMappings();
    }

    @GetMapping("/{id}")
    public FieldMappingDto getFieldMappingById(@PathVariable Long id) {
        return fieldMappingService.getFieldMappingById(id);
    }

    @PostMapping
    public FieldMappingDto createFieldMapping(@RequestBody FieldMappingDto fieldMappingDto) {
        return fieldMappingService.saveFieldMapping(fieldMappingDto);
    }

    @PutMapping("/{id}")
    public FieldMappingDto updateFieldMapping(
            @PathVariable Long id, @RequestBody FieldMappingDto fieldMappingDto) {
        fieldMappingDto.setId(id);
        return fieldMappingService.saveFieldMapping(fieldMappingDto);
    }

    @DeleteMapping("/{id}")
    public void deleteFieldMapping(@PathVariable Long id) {
        fieldMappingService.deleteFieldMapping(id);
    }

}
