package com.example.datasynchronizationtool.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom health indicator that checks connectivity to all configured datasources
 * used for data synchronization.
 */
@Component
public class DataSourceHealthIndicator implements HealthIndicator {

    private final DataSource primaryDataSource;
    private final DataSource secondaryDataSource;

    public DataSourceHealthIndicator(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.secondaryDataSource = secondaryDataSource;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        boolean allHealthy = true;

        // Check primary datasource
        DataSourceStatus primaryStatus = checkDataSource("primary", primaryDataSource);
        details.put("primary", primaryStatus.toMap());
        if (!primaryStatus.isHealthy()) {
            allHealthy = false;
        }

        // Check secondary datasource
        DataSourceStatus secondaryStatus = checkDataSource("secondary", secondaryDataSource);
        details.put("secondary", secondaryStatus.toMap());
        if (!secondaryStatus.isHealthy()) {
            allHealthy = false;
        }

        if (allHealthy) {
            return Health.up()
                    .withDetails(details)
                    .build();
        } else {
            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }

    private DataSourceStatus checkDataSource(String name, DataSource dataSource) {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5); // 5 second timeout
            long responseTime = System.currentTimeMillis() - startTime;

            if (valid) {
                return new DataSourceStatus(true, "Connected", responseTime, null);
            } else {
                return new DataSourceStatus(false, "Connection invalid", responseTime, null);
            }
        } catch (SQLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new DataSourceStatus(false, "Connection failed", responseTime, e.getMessage());
        }
    }

    private static class DataSourceStatus {
        private final boolean healthy;
        private final String status;
        private final long responseTimeMs;
        private final String error;

        DataSourceStatus(boolean healthy, String status, long responseTimeMs, String error) {
            this.healthy = healthy;
            this.status = status;
            this.responseTimeMs = responseTimeMs;
            this.error = error;
        }

        boolean isHealthy() {
            return healthy;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", status);
            map.put("responseTimeMs", responseTimeMs);
            if (error != null) {
                map.put("error", error);
            }
            return map;
        }
    }
}
