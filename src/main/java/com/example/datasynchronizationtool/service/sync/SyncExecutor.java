package com.example.datasynchronizationtool.service.sync;

import com.example.datasynchronizationtool.exception.InvalidConfigurationException;
import com.example.datasynchronizationtool.exception.SyncFailedException;
import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.repository.data.GenericDataRepository;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core synchronization executor that performs the actual data synchronization
 * between source and target systems based on field mappings.
 */
@Component
public class SyncExecutor {

    private static final Logger log = LoggerFactory.getLogger(SyncExecutor.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final GenericDataRepository dataRepository;
    private final TransformationEngine transformationEngine;
    private final DataSourceRegistry dataSourceRegistry;

    @Autowired
    public SyncExecutor(GenericDataRepository dataRepository,
                        TransformationEngine transformationEngine,
                        DataSourceRegistry dataSourceRegistry) {
        this.dataRepository = dataRepository;
        this.transformationEngine = transformationEngine;
        this.dataSourceRegistry = dataSourceRegistry;
    }

    /**
     * Execute synchronization for the given configuration.
     *
     * @param config The sync configuration to execute
     * @return SyncResult containing statistics and any errors
     */
    @Transactional
    public SyncResult executeSynchronization(SyncConfigurationDto config) {
        log.info("Starting synchronization execution for: {} (id: {})",
                config.getName(), config.getId());

        SyncResult result = new SyncResult(config.getId(), config.getName());

        try {
            // Validate configuration
            validateConfiguration(config);

            // Get source and target system info
            SystemInfo sourceInfo = parseSystemInfo(config.getSourceSystem());
            SystemInfo targetInfo = parseSystemInfo(config.getTargetSystem());

            log.debug("Source system: {}, Target system: {}", sourceInfo, targetInfo);

            // Group field mappings by their table relationships
            Map<String, List<FieldMapping>> mappingsBySourceTable = groupMappingsBySourceTable(
                    config.getFieldMappings());

            // Determine batch size
            int batchSize = config.getBatchSize() != null && config.getBatchSize() > 0
                    ? config.getBatchSize()
                    : DEFAULT_BATCH_SIZE;

            // Process each table
            for (Map.Entry<String, List<FieldMapping>> entry : mappingsBySourceTable.entrySet()) {
                String sourceTable = entry.getKey();
                List<FieldMapping> mappings = entry.getValue();

                processTableSync(sourceInfo, targetInfo, sourceTable, mappings, result, batchSize);
            }

            result.complete();
            log.info("Synchronization completed: {}", result.getSummary());

        } catch (InvalidConfigurationException e) {
            log.error("Invalid configuration: {}", e.getMessage());
            result.addError(null, "Configuration error: " + e.getMessage(), e);
            result.complete();
            throw e;
        } catch (Exception e) {
            log.error("Synchronization failed with exception", e);
            result.addError(null, "Unexpected error: " + e.getMessage(), e);
            result.complete();
            throw new SyncFailedException(config.getId(), config.getName(), e.getMessage(), e);
        }

        return result;
    }

    private void validateConfiguration(SyncConfigurationDto config) {
        if (config.getSourceSystem() == null || config.getSourceSystem().trim().isEmpty()) {
            throw new InvalidConfigurationException("Source system is not configured");
        }
        if (config.getTargetSystem() == null || config.getTargetSystem().trim().isEmpty()) {
            throw new InvalidConfigurationException("Target system is not configured");
        }
        if (config.getFieldMappings() == null || config.getFieldMappings().isEmpty()) {
            throw new InvalidConfigurationException("No field mappings configured");
        }

        // Validate datasources are available
        SystemInfo sourceInfo = parseSystemInfo(config.getSourceSystem());
        SystemInfo targetInfo = parseSystemInfo(config.getTargetSystem());

        if (!dataSourceRegistry.isDataSourceAvailable(sourceInfo.systemName)) {
            throw new InvalidConfigurationException(
                    "Datasource not available for source system: " + sourceInfo.systemName);
        }
        if (!dataSourceRegistry.isDataSourceAvailable(targetInfo.systemName)) {
            throw new InvalidConfigurationException(
                    "Datasource not available for target system: " + targetInfo.systemName);
        }

        // Validate field mappings
        for (FieldMapping mapping : config.getFieldMappings()) {
            if (mapping.getSourceField() == null || mapping.getSourceField().trim().isEmpty()) {
                throw new InvalidConfigurationException("Source field is empty in field mapping");
            }
            if (mapping.getTargetField() == null || mapping.getTargetField().trim().isEmpty()) {
                throw new InvalidConfigurationException("Target field is empty in field mapping");
            }

            // Validate transformation if specified
            if (mapping.getTransformation() != null && !mapping.getTransformation().trim().isEmpty()) {
                if (!transformationEngine.isValidTransformation(mapping.getTransformation())) {
                    log.warn("Unknown transformation '{}' for field mapping {} -> {}",
                            mapping.getTransformation(), mapping.getSourceField(), mapping.getTargetField());
                }
            }
        }
    }

    /**
     * Process synchronization for a single source table with pagination support.
     */
    private void processTableSync(SystemInfo sourceInfo, SystemInfo targetInfo,
                                   String sourceTable, List<FieldMapping> mappings,
                                   SyncResult result, int batchSize) {
        log.debug("Processing table sync: {} -> {} (batch size: {})", sourceTable, getTargetTable(mappings), batchSize);

        // Extract source fields to read
        List<String> sourceFields = mappings.stream()
                .map(m -> extractFieldName(m.getSourceField()))
                .distinct()
                .collect(Collectors.toList());

        // Add primary key field if not already included
        String primaryKeyField = determinePrimaryKey(mappings);
        if (primaryKeyField != null && !sourceFields.contains(primaryKeyField)) {
            sourceFields.add(0, primaryKeyField);
        }

        // Process data in batches using pagination
        int offset = 0;
        boolean hasMoreData = true;

        while (hasMoreData) {
            List<Map<String, Object>> sourceData;
            try {
                sourceData = dataRepository.readDataPaginated(
                        sourceInfo.systemName, sourceTable, sourceFields, null, null, offset, batchSize);
                result.incrementRecordsRead(sourceData.size());
                log.debug("Read batch of {} records from source table {} (offset: {})",
                        sourceData.size(), sourceTable, offset);
            } catch (Exception e) {
                log.error("Failed to read data from source: {}", e.getMessage());
                result.addError(sourceTable, "Failed to read source data: " + e.getMessage(), e);
                return;
            }

            if (sourceData.isEmpty()) {
                if (offset == 0) {
                    log.debug("No data to synchronize from table {}", sourceTable);
                    result.addWarning("No data found in source table: " + sourceTable);
                }
                hasMoreData = false;
                continue;
            }

            // Process each record in the batch
            for (Map<String, Object> sourceRecord : sourceData) {
                try {
                    processRecord(sourceInfo, targetInfo, sourceRecord, mappings, primaryKeyField, result);
                } catch (Exception e) {
                    log.error("Failed to process record: {}", e.getMessage());
                    result.addError(sourceTable, "Failed to process record: " + e.getMessage(), e);
                    result.incrementRecordsFailed();
                }
            }

            // Check if we've processed all data
            if (sourceData.size() < batchSize) {
                hasMoreData = false;
            } else {
                offset += batchSize;
            }
        }

        // Process bi-directional sync if any mappings require it
        List<FieldMapping> biDirectionalMappings = mappings.stream()
                .filter(FieldMapping::isBiDirectional)
                .collect(Collectors.toList());

        if (!biDirectionalMappings.isEmpty()) {
            processReverseSync(sourceInfo, targetInfo, sourceTable, biDirectionalMappings, result);
        }
    }

    /**
     * Process a single record synchronization.
     */
    private void processRecord(SystemInfo sourceInfo, SystemInfo targetInfo,
                                Map<String, Object> sourceRecord,
                                List<FieldMapping> mappings,
                                String primaryKeyField,
                                SyncResult result) {
        // Build target record by applying mappings and transformations
        Map<String, Object> targetRecord = new LinkedHashMap<>();
        String targetTable = getTargetTable(mappings);

        for (FieldMapping mapping : mappings) {
            String sourceFieldName = extractFieldName(mapping.getSourceField());
            String targetFieldName = extractFieldName(mapping.getTargetField());

            Object sourceValue = sourceRecord.get(sourceFieldName);

            // Apply transformation if configured
            Object transformedValue = transformationEngine.applyTransformation(
                    sourceValue, mapping.getTransformation());

            targetRecord.put(targetFieldName, transformedValue);
            result.incrementFieldsProcessed();

            if (mapping.isBiDirectional()) {
                result.incrementBiDirectionalFieldsProcessed();
            }

            log.trace("Mapped {} -> {}: {} -> {}",
                    sourceFieldName, targetFieldName, sourceValue, transformedValue);
        }

        // Determine target primary key field
        String targetPrimaryKey = mapPrimaryKeyToTarget(primaryKeyField, mappings);

        // Upsert the record to target
        try {
            boolean inserted = dataRepository.upsertData(
                    targetInfo.systemName, targetTable, targetRecord, targetPrimaryKey);

            if (inserted) {
                result.incrementRecordsWritten();
                log.trace("Inserted new record into {}", targetTable);
            } else {
                result.incrementRecordsUpdated();
                log.trace("Updated existing record in {}", targetTable);
            }
        } catch (Exception e) {
            log.error("Failed to write record to target: {}", e.getMessage());
            result.addError(targetTable, "Failed to write record: " + e.getMessage(), e);
            result.incrementRecordsFailed();
        }
    }

    /**
     * Process reverse (bi-directional) synchronization.
     */
    private void processReverseSync(SystemInfo sourceInfo, SystemInfo targetInfo,
                                     String sourceTable, List<FieldMapping> biDirectionalMappings,
                                     SyncResult result) {
        log.debug("Processing reverse sync for {} bi-directional mappings", biDirectionalMappings.size());

        String targetTable = getTargetTable(biDirectionalMappings);

        // Extract target fields to read
        List<String> targetFields = biDirectionalMappings.stream()
                .map(m -> extractFieldName(m.getTargetField()))
                .distinct()
                .collect(Collectors.toList());

        // Determine primary key for reverse sync
        String targetPrimaryKey = determineTargetPrimaryKey(biDirectionalMappings);
        if (targetPrimaryKey != null && !targetFields.contains(targetPrimaryKey)) {
            targetFields.add(0, targetPrimaryKey);
        }

        // Read data from target (which becomes our source for reverse sync)
        List<Map<String, Object>> targetData;
        try {
            targetData = dataRepository.readData(targetInfo.systemName, targetTable, targetFields);
            log.debug("Read {} records from target table {} for reverse sync", targetData.size(), targetTable);
        } catch (Exception e) {
            log.error("Failed to read data from target for reverse sync: {}", e.getMessage());
            result.addError(targetTable, "Failed to read target data for reverse sync: " + e.getMessage(), e);
            return;
        }

        // Process reverse mappings (swap source and target)
        for (Map<String, Object> targetRecord : targetData) {
            try {
                Map<String, Object> reverseRecord = new LinkedHashMap<>();

                for (FieldMapping mapping : biDirectionalMappings) {
                    String targetFieldName = extractFieldName(mapping.getTargetField());
                    String sourceFieldName = extractFieldName(mapping.getSourceField());

                    Object targetValue = targetRecord.get(targetFieldName);

                    // Apply reverse transformation (if applicable)
                    // For now, we don't apply transformation in reverse
                    // TODO: Support reverse transformations
                    reverseRecord.put(sourceFieldName, targetValue);
                }

                String sourcePrimaryKey = determinePrimaryKey(biDirectionalMappings);
                dataRepository.upsertData(sourceInfo.systemName, sourceTable, reverseRecord, sourcePrimaryKey);

            } catch (Exception e) {
                log.error("Failed to process reverse sync record: {}", e.getMessage());
                result.addError(sourceTable, "Failed reverse sync: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Parse system info from system string.
     * Format: "systemName" or "systemName.tableName"
     */
    private SystemInfo parseSystemInfo(String systemString) {
        if (systemString == null || systemString.trim().isEmpty()) {
            throw new InvalidConfigurationException("System string is empty");
        }

        String[] parts = systemString.split("\\.", 2);
        String systemName = parts[0].trim();
        String defaultTable = parts.length > 1 ? parts[1].trim() : null;

        return new SystemInfo(systemName, defaultTable);
    }

    /**
     * Extract field name from field specification.
     * Supports formats: "fieldName" or "tableName.fieldName"
     */
    private String extractFieldName(String fieldSpec) {
        if (fieldSpec == null) return null;
        String[] parts = fieldSpec.split("\\.");
        return parts[parts.length - 1].trim();
    }

    /**
     * Extract table name from field specification if present.
     */
    private String extractTableName(String fieldSpec) {
        if (fieldSpec == null) return null;
        String[] parts = fieldSpec.split("\\.");
        return parts.length > 1 ? parts[0].trim() : null;
    }

    /**
     * Group field mappings by source table.
     */
    private Map<String, List<FieldMapping>> groupMappingsBySourceTable(List<FieldMapping> mappings) {
        Map<String, List<FieldMapping>> grouped = new LinkedHashMap<>();

        for (FieldMapping mapping : mappings) {
            String sourceTable = extractTableName(mapping.getSourceField());
            if (sourceTable == null) {
                sourceTable = "default";
            }
            grouped.computeIfAbsent(sourceTable, k -> new ArrayList<>()).add(mapping);
        }

        return grouped;
    }

    /**
     * Get the target table from field mappings.
     */
    private String getTargetTable(List<FieldMapping> mappings) {
        for (FieldMapping mapping : mappings) {
            String table = extractTableName(mapping.getTargetField());
            if (table != null) {
                return table;
            }
        }
        return "default";
    }

    /**
     * Determine the primary key field from mappings.
     * Priority: 1) Explicitly marked primary key field
     *           2) Field named "id"
     *           3) Field ending with "_id"
     *           4) Default to "id"
     */
    private String determinePrimaryKey(List<FieldMapping> mappings) {
        // First, look for explicitly marked primary key
        for (FieldMapping mapping : mappings) {
            if (mapping.isPrimaryKey()) {
                String sourceField = extractFieldName(mapping.getSourceField());
                log.debug("Using explicitly configured primary key: {}", sourceField);
                return sourceField;
            }
        }

        // Fall back to naming convention: look for "id" field
        for (FieldMapping mapping : mappings) {
            String sourceField = extractFieldName(mapping.getSourceField());
            if ("id".equalsIgnoreCase(sourceField)) {
                return sourceField;
            }
        }

        // Try to find a field ending with _id
        for (FieldMapping mapping : mappings) {
            String sourceField = extractFieldName(mapping.getSourceField());
            if (sourceField != null && sourceField.toLowerCase().endsWith("_id")) {
                return sourceField;
            }
        }

        // Default to "id"
        log.warn("No primary key found in mappings, defaulting to 'id'");
        return "id";
    }

    /**
     * Determine the target primary key field.
     * Priority: 1) Explicitly marked primary key field
     *           2) Field named "id"
     *           3) Default to "id"
     */
    private String determineTargetPrimaryKey(List<FieldMapping> mappings) {
        // First, look for explicitly marked primary key
        for (FieldMapping mapping : mappings) {
            if (mapping.isPrimaryKey()) {
                return extractFieldName(mapping.getTargetField());
            }
        }

        // Fall back to naming convention
        for (FieldMapping mapping : mappings) {
            String targetField = extractFieldName(mapping.getTargetField());
            if ("id".equalsIgnoreCase(targetField)) {
                return targetField;
            }
        }
        return "id";
    }

    /**
     * Map source primary key to its target equivalent.
     */
    private String mapPrimaryKeyToTarget(String sourcePrimaryKey, List<FieldMapping> mappings) {
        if (sourcePrimaryKey == null) {
            return "id";
        }
        for (FieldMapping mapping : mappings) {
            String sourceField = extractFieldName(mapping.getSourceField());
            if (sourcePrimaryKey.equals(sourceField)) {
                return extractFieldName(mapping.getTargetField());
            }
        }
        return sourcePrimaryKey;
    }

    /**
     * Internal class to hold parsed system information.
     */
    private static class SystemInfo {
        final String systemName;
        final String defaultTable;

        SystemInfo(String systemName, String defaultTable) {
            this.systemName = systemName;
            this.defaultTable = defaultTable;
        }

        @Override
        public String toString() {
            return defaultTable != null
                    ? systemName + "." + defaultTable
                    : systemName;
        }
    }
}
