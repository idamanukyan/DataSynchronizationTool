package com.example.datasynchronizationtool.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncLogDto {

    private Long id;

    @NotNull
    private LocalDateTime timestamp;

    @NotBlank
    private String status;

    @NotBlank
    private String message;

    @NotNull
    private SyncConfigurationDto syncConfiguration;
}
