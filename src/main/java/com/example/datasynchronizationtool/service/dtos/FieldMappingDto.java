package com.example.datasynchronizationtool.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingDto {

    private Long id;

    @NotBlank
    private String sourceField;

    @NotBlank
    private String targetField;

    private String transformation;

    private boolean isBiDirectional;

    private boolean isPrimaryKey;
}
