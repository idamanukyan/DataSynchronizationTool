package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.exception.SyncFailedException;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import com.example.datasynchronizationtool.service.impl.SyncLogServiceImpl;
import com.example.datasynchronizationtool.service.sync.SyncExecutor;
import com.example.datasynchronizationtool.service.sync.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing and executing data synchronization operations.
 * Provides async execution and logging of synchronization events.
 */
@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    private final SyncLogServiceImpl syncLogService;
    private final SyncExecutor syncExecutor;

    @Autowired
    public DataSyncService(SyncLogServiceImpl syncLogService, SyncExecutor syncExecutor) {
        this.syncLogService = syncLogService;
        this.syncExecutor = syncExecutor;
    }

    /**
     * Start asynchronous synchronization for the given configuration.
     *
     * @param syncConfiguration The configuration defining how to synchronize
     */
    @Async
    public void startSynchronization(SyncConfigurationDto syncConfiguration) {
        log.info("Starting synchronization for configuration: {} (id: {})",
                syncConfiguration.getName(), syncConfiguration.getId());

        SyncResult result = null;

        try {
            // Validate basic configuration
            if (syncConfiguration.getFieldMappings() == null || syncConfiguration.getFieldMappings().isEmpty()) {
                log.warn("No field mappings found for configuration: {}", syncConfiguration.getName());
                logSynchronizationEvent(syncConfiguration, "WARNING", "No field mappings configured");
                return;
            }

            // Execute the synchronization
            result = syncExecutor.executeSynchronization(syncConfiguration);

            // Log the result
            String status = mapResultStatus(result.getStatus());
            String message = result.getSummary();

            log.info("Synchronization completed for configuration: {} - Status: {}, Message: {}",
                    syncConfiguration.getName(), status, message);

            logSynchronizationEvent(syncConfiguration, status, message);

            // Log individual errors if any
            if (result.hasErrors()) {
                for (SyncResult.SyncError error : result.getErrors()) {
                    log.warn("Sync error for field '{}': {}", error.getField(), error.getMessage());
                }
            }

        } catch (SyncFailedException e) {
            String errorMessage = "Synchronization failed: " + e.getMessage();
            log.error("Synchronization failed for configuration: {} (id: {})",
                    syncConfiguration.getName(), syncConfiguration.getId(), e);
            logSynchronizationEvent(syncConfiguration, "ERROR", errorMessage);
            throw e;

        } catch (Exception e) {
            String errorMessage = "Unexpected error during synchronization: " + e.getMessage();
            log.error("Unexpected error during synchronization for configuration: {} (id: {})",
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

    /**
     * Start synchronization and return a future for tracking completion.
     *
     * @param syncConfiguration The configuration defining how to synchronize
     * @return CompletableFuture containing the sync result
     */
    @Async
    public CompletableFuture<SyncResult> startSynchronizationWithResult(SyncConfigurationDto syncConfiguration) {
        log.info("Starting synchronization with result tracking for configuration: {} (id: {})",
                syncConfiguration.getName(), syncConfiguration.getId());

        try {
            if (syncConfiguration.getFieldMappings() == null || syncConfiguration.getFieldMappings().isEmpty()) {
                log.warn("No field mappings found for configuration: {}", syncConfiguration.getName());
                logSynchronizationEvent(syncConfiguration, "WARNING", "No field mappings configured");

                SyncResult emptyResult = new SyncResult(syncConfiguration.getId(), syncConfiguration.getName());
                emptyResult.addWarning("No field mappings configured");
                emptyResult.complete();
                return CompletableFuture.completedFuture(emptyResult);
            }

            SyncResult result = syncExecutor.executeSynchronization(syncConfiguration);

            String status = mapResultStatus(result.getStatus());
            logSynchronizationEvent(syncConfiguration, status, result.getSummary());

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Synchronization failed for configuration: {} (id: {})",
                    syncConfiguration.getName(), syncConfiguration.getId(), e);
            logSynchronizationEvent(syncConfiguration, "ERROR", "Synchronization failed: " + e.getMessage());

            return CompletableFuture.failedFuture(new SyncFailedException(
                    syncConfiguration.getId(),
                    syncConfiguration.getName(),
                    e.getMessage(),
                    e
            ));
        }
    }

    /**
     * Execute synchronization synchronously (blocking).
     *
     * @param syncConfiguration The configuration defining how to synchronize
     * @return SyncResult containing statistics and any errors
     */
    public SyncResult executeSynchronizationSync(SyncConfigurationDto syncConfiguration) {
        log.info("Executing synchronous synchronization for configuration: {} (id: {})",
                syncConfiguration.getName(), syncConfiguration.getId());

        try {
            if (syncConfiguration.getFieldMappings() == null || syncConfiguration.getFieldMappings().isEmpty()) {
                log.warn("No field mappings found for configuration: {}", syncConfiguration.getName());
                logSynchronizationEvent(syncConfiguration, "WARNING", "No field mappings configured");

                SyncResult emptyResult = new SyncResult(syncConfiguration.getId(), syncConfiguration.getName());
                emptyResult.addWarning("No field mappings configured");
                emptyResult.complete();
                return emptyResult;
            }

            SyncResult result = syncExecutor.executeSynchronization(syncConfiguration);

            String status = mapResultStatus(result.getStatus());
            logSynchronizationEvent(syncConfiguration, status, result.getSummary());

            return result;

        } catch (Exception e) {
            log.error("Synchronization failed for configuration: {} (id: {})",
                    syncConfiguration.getName(), syncConfiguration.getId(), e);
            logSynchronizationEvent(syncConfiguration, "ERROR", "Synchronization failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Map SyncResult status to log status string.
     */
    private String mapResultStatus(SyncResult.SyncStatus status) {
        switch (status) {
            case SUCCESS:
                return "SUCCESS";
            case PARTIAL_SUCCESS:
                return "WARNING";
            case NO_DATA:
                return "INFO";
            case FAILED:
            default:
                return "ERROR";
        }
    }

    /**
     * Log a synchronization event to the database.
     */
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
