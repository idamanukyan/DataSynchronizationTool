package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncConfiguration;
import com.example.datasynchronizationtool.repository.SyncConfigurationRepository;
import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncConfigServiceImpl implements ISyncConfigurationService {

    private final SyncConfigurationRepository syncConfigurationRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SyncConfigServiceImpl(SyncConfigurationRepository syncConfigurationRepository, ModelMapper modelMapper) {
        this.syncConfigurationRepository = syncConfigurationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<SyncConfigurationDto> getAllSyncConfigurations() {
        return syncConfigurationRepository.findAll().stream()
                .map(syncConfiguration -> modelMapper.map(syncConfiguration, SyncConfigurationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SyncConfigurationDto getSyncConfigurationById(Long id) {
        return syncConfigurationRepository.findById(id)
                .map(syncConfiguration -> modelMapper.map(syncConfiguration, SyncConfigurationDto.class))
                .orElse(null);
    }

    @Override
    public SyncConfigurationDto saveSyncConfiguration(SyncConfigurationDto syncConfigurationDto) {
        SyncConfiguration syncConfiguration = modelMapper.map(syncConfigurationDto, SyncConfiguration.class);
        SyncConfiguration saved = syncConfigurationRepository.save(syncConfiguration);
        return modelMapper.map(saved, SyncConfigurationDto.class);
    }

    @Override
    public void deleteSyncConfiguration(Long id) {
        syncConfigurationRepository.deleteById(id);
    }
}