package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;

import java.util.List;

public interface ISyncConfigurationService {

    List<SyncConfigurationDto> getAllSyncConfigurations();

    SyncConfigurationDto getSyncConfigurationById(Long id);

    SyncConfigurationDto saveSyncConfiguration(SyncConfigurationDto syncConfiguration);

    void deleteSyncConfiguration(Long id);
}
