package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sync-configurations")
public class SyncConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigurationController.class);

    private final ISyncConfigurationService syncConfigurationService;

    @Autowired
    public SyncConfigurationController(ISyncConfigurationService syncConfigurationService) {
        this.syncConfigurationService = syncConfigurationService;
    }

    @GetMapping
    public ResponseEntity<List<SyncConfigurationDto>> getAllSyncConfigurations() {
        log.debug("GET request to fetch all sync configurations");
        List<SyncConfigurationDto> configurations = syncConfigurationService.getAllSyncConfigurations();
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyncConfigurationDto> getSyncConfigurationById(@PathVariable Long id) {
        log.debug("GET request to fetch sync configuration with id: {}", id);
        SyncConfigurationDto configuration = syncConfigurationService.getSyncConfigurationById(id);
        return ResponseEntity.ok(configuration);
    }

    @PostMapping
    public ResponseEntity<SyncConfigurationDto> createSyncConfiguration(
            @Valid @RequestBody SyncConfigurationDto syncConfigurationDto) {
        log.debug("POST request to create sync configuration: {}", syncConfigurationDto.getName());
        SyncConfigurationDto created = syncConfigurationService.saveSyncConfiguration(syncConfigurationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SyncConfigurationDto> updateSyncConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody SyncConfigurationDto syncConfigurationDto) {
        log.debug("PUT request to update sync configuration with id: {}", id);
        syncConfigurationDto.setId(id);
        SyncConfigurationDto updated = syncConfigurationService.saveSyncConfiguration(syncConfigurationDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyncConfiguration(@PathVariable Long id) {
        log.debug("DELETE request for sync configuration with id: {}", id);
        syncConfigurationService.deleteSyncConfiguration(id);
        return ResponseEntity.noContent().build();
    }
}
