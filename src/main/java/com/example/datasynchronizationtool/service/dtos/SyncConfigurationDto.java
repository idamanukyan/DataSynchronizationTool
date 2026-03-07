package com.example.datasynchronizationtool.service.dtos;

import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.model.SyncSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigurationDto {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String sourceSystem;

    @NotBlank
    private String targetSystem;

    private boolean isActive;

    private List<FieldMapping> fieldMappings;

    private SyncSchedule syncSchedule;
}
