package com.example.datasynchronizationtool;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test that requires a full database setup.
 * Disabled by default to allow unit tests to run independently.
 */
class DataSynchronizationToolApplicationTests {

    @Test
    @Disabled("Requires database setup - run with 'gradle integrationTest' when DB is configured")
    void contextLoads() {
        // This test verifies the Spring context loads correctly
        // Requires MySQL databases to be running with proper configuration
    }

}


