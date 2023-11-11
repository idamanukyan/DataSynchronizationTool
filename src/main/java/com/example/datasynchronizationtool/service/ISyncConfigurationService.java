package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.model.SyncConfiguration;

import java.util.List;
import java.util.Optional;

public interface ISyncConfigurationService {

    List<SyncConfiguration> getAllSyncConfigurations();

    Optional<SyncConfiguration> getSyncConfigurationById(Long id);

    SyncConfiguration saveSyncConfiguration(SyncConfiguration syncConfiguration);

    void deleteSyncConfiguration(Long id);
}
