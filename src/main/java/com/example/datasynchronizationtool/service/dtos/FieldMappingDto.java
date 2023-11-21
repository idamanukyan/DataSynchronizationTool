package com.example.datasynchronizationtool.service.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class FieldMappingDto {

    private Long id;
    @NotEmpty
    @NotNull
    private String sourceField;
    @NotEmpty
    @NotNull
    private String targetField;
    @NotEmpty
    @NotNull
    private String transformation;
    @NotEmpty
    @NotNull
    private boolean isBiDirectional;

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }
}
