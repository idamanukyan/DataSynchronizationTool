package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.exception.ResourceNotFoundException;
import com.example.datasynchronizationtool.model.SyncConfiguration;
import com.example.datasynchronizationtool.repository.SyncConfigurationRepository;
import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncConfigServiceImpl implements ISyncConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigServiceImpl.class);

    private final SyncConfigurationRepository syncConfigurationRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SyncConfigServiceImpl(SyncConfigurationRepository syncConfigurationRepository, ModelMapper modelMapper) {
        this.syncConfigurationRepository = syncConfigurationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncConfigurationDto> getAllSyncConfigurations() {
        log.debug("Fetching all sync configurations");
        List<SyncConfigurationDto> configurations = syncConfigurationRepository.findAll().stream()
                .map(syncConfiguration -> modelMapper.map(syncConfiguration, SyncConfigurationDto.class))
                .collect(Collectors.toList());
        log.info("Retrieved {} sync configurations", configurations.size());
        return configurations;
    }

    @Override
    @Transactional(readOnly = true)
    public SyncConfigurationDto getSyncConfigurationById(Long id) {
        log.debug("Fetching sync configuration with id: {}", id);
        return syncConfigurationRepository.findById(id)
                .map(syncConfiguration -> {
                    log.info("Found sync configuration: {} (id: {})", syncConfiguration.getName(), id);
                    return modelMapper.map(syncConfiguration, SyncConfigurationDto.class);
                })
                .orElseThrow(() -> {
                    log.warn("Sync configuration not found with id: {}", id);
                    return new ResourceNotFoundException("SyncConfiguration", "id", id);
                });
    }

    @Override
    @Transactional
    public SyncConfigurationDto saveSyncConfiguration(SyncConfigurationDto syncConfigurationDto) {
        log.debug("Saving sync configuration: {}", syncConfigurationDto.getName());
        SyncConfiguration syncConfiguration = modelMapper.map(syncConfigurationDto, SyncConfiguration.class);
        SyncConfiguration saved = syncConfigurationRepository.save(syncConfiguration);
        log.info("Saved sync configuration: {} (id: {})", saved.getName(), saved.getId());
        return modelMapper.map(saved, SyncConfigurationDto.class);
    }

    @Override
    @Transactional
    public void deleteSyncConfiguration(Long id) {
        log.debug("Deleting sync configuration with id: {}", id);
        if (!syncConfigurationRepository.existsById(id)) {
            log.warn("Cannot delete - sync configuration not found with id: {}", id);
            throw new ResourceNotFoundException("SyncConfiguration", "id", id);
        }
        syncConfigurationRepository.deleteById(id);
        log.info("Deleted sync configuration with id: {}", id);
    }
}
