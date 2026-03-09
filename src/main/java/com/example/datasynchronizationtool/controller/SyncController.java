package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.DataSyncService;
import com.example.datasynchronizationtool.service.ISyncConfigurationService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import com.example.datasynchronizationtool.service.sync.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final DataSyncService dataSyncService;
    private final ISyncConfigurationService syncConfigurationService;

    @Autowired
    public SyncController(DataSyncService dataSyncService,
                          ISyncConfigurationService syncConfigurationService) {
        this.dataSyncService = dataSyncService;
        this.syncConfigurationService = syncConfigurationService;
    }

    /**
     * Start asynchronous synchronization with provided configuration.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSynchronization(
            @Valid @RequestBody SyncConfigurationDto syncConfiguration) {
        log.info("POST request to start synchronization for configuration: {}", syncConfiguration.getName());
        dataSyncService.startSynchronization(syncConfiguration);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Synchronization started asynchronously");
        response.put("configuration", syncConfiguration.getName());

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Start synchronization for an existing configuration by ID.
     */
    @PostMapping("/start/{configurationId}")
    public ResponseEntity<Map<String, String>> startSynchronizationById(
            @PathVariable Long configurationId) {
        log.info("POST request to start synchronization for configuration id: {}", configurationId);

        SyncConfigurationDto config = syncConfigurationService.getSyncConfigurationById(configurationId);
        dataSyncService.startSynchronization(config);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Synchronization started asynchronously");
        response.put("configurationId", String.valueOf(configurationId));
        response.put("configurationName", config.getName());

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Execute synchronization synchronously and return results.
     */
    @PostMapping("/execute")
    public ResponseEntity<SyncResultResponse> executeSynchronization(
            @Valid @RequestBody SyncConfigurationDto syncConfiguration) {
        log.info("POST request to execute synchronization for configuration: {}", syncConfiguration.getName());

        SyncResult result = dataSyncService.executeSynchronizationSync(syncConfiguration);

        return ResponseEntity.ok(SyncResultResponse.from(result));
    }

    /**
     * Execute synchronization synchronously for an existing configuration by ID.
     */
    @PostMapping("/execute/{configurationId}")
    public ResponseEntity<SyncResultResponse> executeSynchronizationById(
            @PathVariable Long configurationId) {
        log.info("POST request to execute synchronization for configuration id: {}", configurationId);

        SyncConfigurationDto config = syncConfigurationService.getSyncConfigurationById(configurationId);
        SyncResult result = dataSyncService.executeSynchronizationSync(config);

        return ResponseEntity.ok(SyncResultResponse.from(result));
    }

    /**
     * Backwards compatibility endpoint - redirects to /api/sync/start
     */
    @PostMapping("/start-synchronization")
    public ResponseEntity<Map<String, String>> startSynchronizationLegacy(
            @Valid @RequestBody SyncConfigurationDto syncConfiguration) {
        return startSynchronization(syncConfiguration);
    }

    /**
     * Response DTO for sync results.
     */
    public static class SyncResultResponse {
        private Long configurationId;
        private String configurationName;
        private String status;
        private String summary;
        private int recordsRead;
        private int recordsWritten;
        private int recordsUpdated;
        private int recordsSkipped;
        private int recordsFailed;
        private int fieldsProcessed;
        private int biDirectionalFieldsProcessed;
        private long durationMillis;
        private int errorCount;
        private int warningCount;

        public static SyncResultResponse from(SyncResult result) {
            SyncResultResponse response = new SyncResultResponse();
            response.configurationId = result.getConfigurationId();
            response.configurationName = result.getConfigurationName();
            response.status = result.getStatus().name();
            response.summary = result.getSummary();
            response.recordsRead = result.getRecordsRead();
            response.recordsWritten = result.getRecordsWritten();
            response.recordsUpdated = result.getRecordsUpdated();
            response.recordsSkipped = result.getRecordsSkipped();
            response.recordsFailed = result.getRecordsFailed();
            response.fieldsProcessed = result.getFieldsProcessed();
            response.biDirectionalFieldsProcessed = result.getBiDirectionalFieldsProcessed();
            response.durationMillis = result.getDuration().toMillis();
            response.errorCount = result.getErrors().size();
            response.warningCount = result.getWarnings().size();
            return response;
        }

        // Getters
        public Long getConfigurationId() { return configurationId; }
        public String getConfigurationName() { return configurationName; }
        public String getStatus() { return status; }
        public String getSummary() { return summary; }
        public int getRecordsRead() { return recordsRead; }
        public int getRecordsWritten() { return recordsWritten; }
        public int getRecordsUpdated() { return recordsUpdated; }
        public int getRecordsSkipped() { return recordsSkipped; }
        public int getRecordsFailed() { return recordsFailed; }
        public int getFieldsProcessed() { return fieldsProcessed; }
        public int getBiDirectionalFieldsProcessed() { return biDirectionalFieldsProcessed; }
        public long getDurationMillis() { return durationMillis; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
    }
}
