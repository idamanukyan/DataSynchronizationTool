package com.example.datasynchronizationtool.metrics;

import com.example.datasynchronizationtool.service.sync.SyncResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking synchronization metrics using Micrometer.
 * Exposes metrics via Spring Actuator for Prometheus scraping.
 */
@Service
public class SyncMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter syncSuccessCounter;
    private final Counter syncFailureCounter;
    private final Counter recordsReadCounter;
    private final Counter recordsWrittenCounter;
    private final Counter recordsUpdatedCounter;
    private final Counter recordsFailedCounter;

    // Timers
    private final Timer syncDurationTimer;

    // Gauges (using AtomicLong for thread-safety)
    private final AtomicLong activeSyncs;
    private final AtomicLong lastSyncDurationMs;

    public SyncMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.syncSuccessCounter = Counter.builder("sync.executions.success")
                .description("Number of successful synchronization executions")
                .register(meterRegistry);

        this.syncFailureCounter = Counter.builder("sync.executions.failure")
                .description("Number of failed synchronization executions")
                .register(meterRegistry);

        this.recordsReadCounter = Counter.builder("sync.records.read")
                .description("Total number of records read from source")
                .register(meterRegistry);

        this.recordsWrittenCounter = Counter.builder("sync.records.written")
                .description("Total number of records written (inserted)")
                .register(meterRegistry);

        this.recordsUpdatedCounter = Counter.builder("sync.records.updated")
                .description("Total number of records updated")
                .register(meterRegistry);

        this.recordsFailedCounter = Counter.builder("sync.records.failed")
                .description("Total number of records that failed to sync")
                .register(meterRegistry);

        // Initialize timer
        this.syncDurationTimer = Timer.builder("sync.duration")
                .description("Duration of synchronization executions")
                .register(meterRegistry);

        // Initialize gauges
        this.activeSyncs = new AtomicLong(0);
        meterRegistry.gauge("sync.active", activeSyncs);

        this.lastSyncDurationMs = new AtomicLong(0);
        meterRegistry.gauge("sync.last.duration.ms", lastSyncDurationMs);
    }

    /**
     * Record that a sync has started.
     */
    public void recordSyncStarted() {
        activeSyncs.incrementAndGet();
    }

    /**
     * Record sync completion with results.
     */
    public void recordSyncCompleted(SyncResult result) {
        activeSyncs.decrementAndGet();

        // Record success/failure
        if (result.isSuccess()) {
            syncSuccessCounter.increment();
        } else {
            syncFailureCounter.increment();
        }

        // Record duration
        long durationMs = result.getDurationMs();
        syncDurationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        lastSyncDurationMs.set(durationMs);

        // Record record counts
        recordsReadCounter.increment(result.getRecordsRead());
        recordsWrittenCounter.increment(result.getRecordsWritten());
        recordsUpdatedCounter.increment(result.getRecordsUpdated());
        recordsFailedCounter.increment(result.getRecordsFailed());
    }

    /**
     * Record a sync failure (when exception thrown before result available).
     */
    public void recordSyncFailed() {
        activeSyncs.decrementAndGet();
        syncFailureCounter.increment();
    }

    /**
     * Record a sync by configuration name for detailed tracking.
     */
    public void recordSyncByConfiguration(String configName, SyncResult result) {
        // Create configuration-specific counters
        Counter.builder("sync.configuration.executions")
                .tag("config", configName)
                .tag("status", result.isSuccess() ? "success" : "failure")
                .description("Executions by configuration")
                .register(meterRegistry)
                .increment();

        Timer.builder("sync.configuration.duration")
                .tag("config", configName)
                .description("Duration by configuration")
                .register(meterRegistry)
                .record(result.getDurationMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * Get the count of currently active synchronizations.
     */
    public long getActiveSyncCount() {
        return activeSyncs.get();
    }
}
