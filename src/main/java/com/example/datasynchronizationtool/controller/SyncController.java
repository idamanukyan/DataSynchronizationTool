package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.DataSyncService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final DataSyncService dataSyncService;

    @Autowired
    public SyncController(DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    @PostMapping("/start-synchronization")
    public ResponseEntity<Map<String, String>> startSynchronization(
            @Valid @RequestBody SyncConfigurationDto syncConfiguration) {
        log.info("POST request to start synchronization for configuration: {}", syncConfiguration.getName());
        dataSyncService.startSynchronization(syncConfiguration);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Synchronization started asynchronously");
        response.put("configuration", syncConfiguration.getName());

        return ResponseEntity.accepted().body(response);
    }
}
