package com.example.datasynchronizationtool.exception;

public class SyncFailedException extends RuntimeException {

    private final Long configurationId;
    private final String configurationName;

    public SyncFailedException(String message) {
        super(message);
        this.configurationId = null;
        this.configurationName = null;
    }

    public SyncFailedException(String message, Throwable cause) {
        super(message, cause);
        this.configurationId = null;
        this.configurationName = null;
    }

    public SyncFailedException(Long configurationId, String configurationName, String message) {
        super(String.format("Synchronization failed for configuration '%s' (ID: %d): %s",
                configurationName, configurationId, message));
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }

    public SyncFailedException(Long configurationId, String configurationName, String message, Throwable cause) {
        super(String.format("Synchronization failed for configuration '%s' (ID: %d): %s",
                configurationName, configurationId, message), cause);
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }
}
