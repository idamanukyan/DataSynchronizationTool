package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.DataSyncService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SyncController {

    private final DataSyncService dataSyncService;

    @Autowired
    public SyncController(DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    @PostMapping("/start-synchronization")
    public ResponseEntity<String> startSynchronization(@RequestBody SyncConfigurationDto syncConfiguration) {
        dataSyncService.startSynchronization(syncConfiguration);
        return ResponseEntity.ok("Synchronization started asynchronously.");
    }
}
