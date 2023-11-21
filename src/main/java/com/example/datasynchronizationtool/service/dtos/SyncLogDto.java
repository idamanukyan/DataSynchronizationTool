package com.example.datasynchronizationtool.service.dtos;

import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Builder
public class SyncLogDto {

    private Long id;
    @NotEmpty
    @NotNull
    private LocalDateTime timestamp;
    @NotEmpty
    @NotNull
    private String status;
    @NotEmpty
    @NotNull
    private String message;
    @NotEmpty
    @NotNull
    private SyncConfigurationDto syncConfiguration;

    public SyncLogDto() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSyncConfiguration(SyncConfigurationDto syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }
}
