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
public class SyncScheduleDto {

    private Long id;

    @NotBlank
    private String cronExpression;

    private SyncConfigurationDto syncConfiguration;
}
