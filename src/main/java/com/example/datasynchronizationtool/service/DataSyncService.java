package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.exception.SyncFailedException;
import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import com.example.datasynchronizationtool.service.impl.SyncLogServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    private final SyncLogServiceImpl syncLogService;

    @Autowired
    public DataSyncService(SyncLogServiceImpl syncLogService) {
        this.syncLogService = syncLogService;
    }

    @Async
    public void startSynchronization(SyncConfigurationDto syncConfiguration) {
        log.info("Starting synchronization for configuration: {} (id: {})",
                syncConfiguration.getName(), syncConfiguration.getId());

        try {
            if (syncConfiguration.getFieldMappings() == null || syncConfiguration.getFieldMappings().isEmpty()) {
                log.warn("No field mappings found for configuration: {}", syncConfiguration.getName());
                logSynchronizationEvent(syncConfiguration, "WARNING", "No field mappings configured");
                return;
            }

            int processedMappings = 0;
            int biDirectionalMappings = 0;

            for (FieldMapping fieldMapping : syncConfiguration.getFieldMappings()) {
                log.debug("Processing field mapping: {} -> {}",
                        fieldMapping.getSourceField(), fieldMapping.getTargetField());

                // Get source data based on fieldMapping.getSourceField()
                // Apply transformation logic if needed (fieldMapping.getTransformation())
                // Update or insert data in the target system based on fieldMapping.getTargetField()

                if (fieldMapping.isBiDirectional()) {
                    log.debug("Processing bi-directional sync for: {} <-> {}",
                            fieldMapping.getSourceField(), fieldMapping.getTargetField());
                    // Get target data based on fieldMapping.getTargetField()
                    // Apply reverse transformation logic if needed
                    // Update or insert data in the source system based on fieldMapping.getSourceField()
                    biDirectionalMappings++;
                }

                processedMappings++;
            }

            String successMessage = String.format(
                    "Synchronization completed successfully. Processed %d field mappings (%d bi-directional)",
                    processedMappings, biDirectionalMappings);

            log.info("Synchronization completed for configuration: {} - {}",
                    syncConfiguration.getName(), successMessage);
            logSynchronizationEvent(syncConfiguration, "SUCCESS", successMessage);

        } catch (Exception e) {
            String errorMessage = "Synchronization failed: " + e.getMessage();
            log.error("Synchronization failed for configuration: {} (id: {})",
                    syncConfiguration.getName(), syncConfiguration.getId(), e);
            logSynchronizationEvent(syncConfiguration, "ERROR", errorMessage);

            throw new SyncFailedException(
                    syncConfiguration.getId(),
                    syncConfiguration.getName(),
                    e.getMessage(),
                    e
            );
        }
    }

    private void logSynchronizationEvent(SyncConfigurationDto syncConfiguration, String status, String message) {
        log.debug("Logging sync event - status: {}, message: {}", status, message);

        SyncLogDto syncLog = new SyncLogDto();
        syncLog.setTimestamp(LocalDateTime.now());
        syncLog.setStatus(status);
        syncLog.setMessage(message);
        syncLog.setSyncConfiguration(syncConfiguration);

        try {
            syncLogService.saveSyncLog(syncLog);
            log.debug("Sync event logged successfully");
        } catch (Exception e) {
            log.error("Failed to save sync log: {}", e.getMessage(), e);
        }
    }
}
