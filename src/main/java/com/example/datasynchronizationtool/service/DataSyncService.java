package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import com.example.datasynchronizationtool.service.impl.SyncLogServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataSyncService {

    private SyncLogServiceImpl syncLogService;

    @Async
    public void startSynchronization(SyncConfigurationDto syncConfiguration) {
        try {
            // Perform your synchronization logic here

            // Iterate through field mappings and perform data transfer
            for (FieldMapping fieldMapping : syncConfiguration.getFieldMappings()) {
                // Get source data based on fieldMapping.getSourceField()
                // Apply transformation logic if needed (fieldMapping.getTransformation())
                // Update or insert data in the target system based on fieldMapping.getTargetField()
                // If bi-directional synchronization is enabled, update data in the source system
                if (fieldMapping.isBiDirectional()) {
                    // Get target data based on fieldMapping.getTargetField()
                    // Apply reverse transformation logic if needed
                    // Update or insert data in the source system based on fieldMapping.getSourceField()
                    System.out.println();
                }
                logSynchronizationEvent(syncConfiguration, "SUCCESS", "Bi-directional synchronization completed successfully");
            }

            // Log a successful synchronization event
            logSynchronizationEvent(syncConfiguration, "SUCCESS", "Synchronization completed successfully");
        } catch (Exception e) {
            // Log an error if synchronization fails
            logSynchronizationEvent(syncConfiguration, "ERROR", "Synchronization failed: " + e.getMessage());
        }
    }

    private void logSynchronizationEvent(SyncConfigurationDto syncConfiguration, String status, String message) {
        SyncLogDto syncLog = new SyncLogDto();
        syncLog.setTimestamp(LocalDateTime.now());
        syncLog.setStatus(status);
        syncLog.setMessage(message);
        syncLog.setSyncConfiguration(syncConfiguration);
        syncLogService.saveSyncLog(syncLog);
    }
}
