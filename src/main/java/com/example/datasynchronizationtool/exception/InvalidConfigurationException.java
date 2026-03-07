package com.example.datasynchronizationtool.exception;

public class InvalidConfigurationException extends RuntimeException {

    private final String field;

    public InvalidConfigurationException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidConfigurationException(String field, String message) {
        super(String.format("Invalid configuration for field '%s': %s", field, message));
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
