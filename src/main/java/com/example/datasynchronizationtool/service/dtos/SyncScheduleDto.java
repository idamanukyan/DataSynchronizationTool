package com.example.datasynchronizationtool.service.dtos;

import com.example.datasynchronizationtool.model.SyncConfiguration;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class SyncScheduleDto {

    private Long id;
    @NotEmpty
    @NotNull
    private String cronExpression;
    @NotEmpty
    @NotNull
    private SyncConfiguration syncConfiguration;

    public void setId(Long id) {
        this.id = id;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setSyncConfiguration(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }
}
