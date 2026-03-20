package com.example.datasynchronizationtool.service.sync;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a synchronization operation, including statistics and any errors encountered.
 */
public class SyncResult {

    private final Long configurationId;
    private final String configurationName;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    private int recordsRead;
    private int recordsWritten;
    private int recordsUpdated;
    private int recordsSkipped;
    private int recordsFailed;

    private int fieldsProcessed;
    private int biDirectionalFieldsProcessed;

    private final List<SyncError> errors;
    private final List<String> warnings;

    private SyncStatus status;

    public enum SyncStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        NO_DATA
    }

    public SyncResult(Long configurationId, String configurationName) {
        this.configurationId = configurationId;
        this.configurationName = configurationName;
        this.startTime = LocalDateTime.now();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.status = SyncStatus.SUCCESS;
    }

    public void complete() {
        this.endTime = LocalDateTime.now();
        determineStatus();
    }

    private void determineStatus() {
        if (!errors.isEmpty() && recordsWritten == 0 && recordsUpdated == 0) {
            status = SyncStatus.FAILED;
        } else if (!errors.isEmpty()) {
            status = SyncStatus.PARTIAL_SUCCESS;
        } else if (recordsRead == 0) {
            status = SyncStatus.NO_DATA;
        } else {
            status = SyncStatus.SUCCESS;
        }
    }

    public void incrementRecordsRead() {
        this.recordsRead++;
    }

    public void incrementRecordsRead(int count) {
        this.recordsRead += count;
    }

    public void incrementRecordsWritten() {
        this.recordsWritten++;
    }

    public void incrementRecordsUpdated() {
        this.recordsUpdated++;
    }

    public void incrementRecordsSkipped() {
        this.recordsSkipped++;
    }

    public void incrementRecordsFailed() {
        this.recordsFailed++;
    }

    public void incrementFieldsProcessed() {
        this.fieldsProcessed++;
    }

    public void incrementBiDirectionalFieldsProcessed() {
        this.biDirectionalFieldsProcessed++;
    }

    public void addError(String field, String message, Exception exception) {
        errors.add(new SyncError(field, message, exception));
    }

    public void addWarning(String message) {
        warnings.add(message);
    }

    public Duration getDuration() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    public long getDurationMs() {
        return getDuration().toMillis();
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Synchronization ").append(status.name());
        sb.append(" for '").append(configurationName).append("'");
        sb.append(" - Read: ").append(recordsRead);
        sb.append(", Written: ").append(recordsWritten);
        sb.append(", Updated: ").append(recordsUpdated);
        if (recordsSkipped > 0) {
            sb.append(", Skipped: ").append(recordsSkipped);
        }
        if (recordsFailed > 0) {
            sb.append(", Failed: ").append(recordsFailed);
        }
        sb.append(", Fields: ").append(fieldsProcessed);
        if (biDirectionalFieldsProcessed > 0) {
            sb.append(" (").append(biDirectionalFieldsProcessed).append(" bi-directional)");
        }
        sb.append(", Duration: ").append(getDuration().toMillis()).append("ms");
        if (!errors.isEmpty()) {
            sb.append(", Errors: ").append(errors.size());
        }
        return sb.toString();
    }

    // Getters
    public Long getConfigurationId() {
        return configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getRecordsRead() {
        return recordsRead;
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }

    public int getRecordsUpdated() {
        return recordsUpdated;
    }

    public int getRecordsSkipped() {
        return recordsSkipped;
    }

    public int getRecordsFailed() {
        return recordsFailed;
    }

    public int getFieldsProcessed() {
        return fieldsProcessed;
    }

    public int getBiDirectionalFieldsProcessed() {
        return biDirectionalFieldsProcessed;
    }

    public List<SyncError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == SyncStatus.SUCCESS || status == SyncStatus.NO_DATA;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Represents an error that occurred during synchronization.
     */
    public static class SyncError {
        private final String field;
        private final String message;
        private final String exceptionType;
        private final String exceptionMessage;

        public SyncError(String field, String message, Exception exception) {
            this.field = field;
            this.message = message;
            this.exceptionType = exception != null ? exception.getClass().getSimpleName() : null;
            this.exceptionMessage = exception != null ? exception.getMessage() : null;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        @Override
        public String toString() {
            return String.format("SyncError{field='%s', message='%s', exception='%s: %s'}",
                    field, message, exceptionType, exceptionMessage);
        }
    }
}
