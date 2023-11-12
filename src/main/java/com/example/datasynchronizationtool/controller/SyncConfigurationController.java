package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync-configurations")
public class SyncConfigurationController {
    private final ISyncConfigurationService syncConfigurationService;

    @Autowired
    public SyncConfigurationController(ISyncConfigurationService syncConfigurationService) {
        this.syncConfigurationService = syncConfigurationService;
    }

    @GetMapping
    public List<SyncConfigurationDto> getAllSyncConfigurations() {
        return syncConfigurationService.getAllSyncConfigurations();
    }

    @GetMapping("/{id}")
    public SyncConfigurationDto getSyncConfigurationById(@PathVariable Long id) {
        return syncConfigurationService.getSyncConfigurationById(id);
    }

    @PostMapping
    public SyncConfigurationDto createSyncConfiguration(@RequestBody SyncConfigurationDto syncConfigurationDto) {
        return syncConfigurationService.saveSyncConfiguration(syncConfigurationDto);
    }

    @PutMapping("/{id}")
    public SyncConfigurationDto updateSyncConfiguration(
            @PathVariable Long id, @RequestBody SyncConfigurationDto syncConfigurationDto) {
        syncConfigurationDto.setId(id);
        return syncConfigurationService.saveSyncConfiguration(syncConfigurationDto);
    }

    @DeleteMapping("/{id}")
    public void deleteSyncConfiguration(@PathVariable Long id) {
        syncConfigurationService.deleteSyncConfiguration(id);
    }
}
