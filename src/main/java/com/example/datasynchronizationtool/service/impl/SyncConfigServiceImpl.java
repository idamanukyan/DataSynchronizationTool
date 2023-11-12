package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncConfiguration;
import com.example.datasynchronizationtool.repository.SyncConfigurationRepository;
import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class SyncConfigServiceImpl implements ISyncConfigurationService {

    private final SyncConfigurationRepository syncConfigurationRepository;

    @Autowired
    public SyncConfigServiceImpl(SyncConfigurationRepository syncConfigurationRepository) {
        this.syncConfigurationRepository = syncConfigurationRepository;
    }

    @Override
    public List<SyncConfiguration> getAllSyncConfigurations() {
        return syncConfigurationRepository.findAll();
    }

    @Override
    public Optional<SyncConfiguration> getSyncConfigurationById(Long id) {
        return syncConfigurationRepository.findById(id);
    }

    @Override
    public SyncConfiguration saveSyncConfiguration(SyncConfiguration syncConfiguration) {
        return syncConfigurationRepository.save(syncConfiguration);
    }

    @Override
    public void deleteSyncConfiguration(Long id) {
        syncConfigurationRepository.deleteById(id);
    }
}