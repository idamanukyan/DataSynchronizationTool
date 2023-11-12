package com.example.datasynchronizationtool.service.dtos;

import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.model.SyncSchedule;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SyncConfigurationDto {

    private Long id;
    @NotEmpty
    @NotNull
    private String name;
    @NotEmpty
    @NotNull
    private String description;
    @NotEmpty
    @NotNull
    private String sourceSystem;
    @NotEmpty
    @NotNull
    private String targetSystem;
    @NotEmpty
    @NotNull
    private boolean isActive;
    @NotEmpty
    @NotNull
    private List<FieldMapping> fieldMappings;
    @NotEmpty
    @NotNull
    private SyncSchedule syncSchedule;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public void setSyncSchedule(SyncSchedule syncSchedule) {
        this.syncSchedule = syncSchedule;
    }
}
