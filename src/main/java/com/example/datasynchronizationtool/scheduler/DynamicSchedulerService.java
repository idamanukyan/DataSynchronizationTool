package com.example.datasynchronizationtool.scheduler;

import com.example.datasynchronizationtool.exception.InvalidConfigurationException;
import com.example.datasynchronizationtool.model.SyncConfiguration;
import com.example.datasynchronizationtool.repository.SyncConfigurationRepository;
import com.example.datasynchronizationtool.service.DataSyncService;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final SyncConfigurationRepository syncConfigurationRepository;
    private final DataSyncService dataSyncService;
    private final ModelMapper modelMapper;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public DynamicSchedulerService(
            TaskScheduler taskScheduler,
            SyncConfigurationRepository syncConfigurationRepository,
            DataSyncService dataSyncService,
            ModelMapper modelMapper) {
        this.taskScheduler = taskScheduler;
        this.syncConfigurationRepository = syncConfigurationRepository;
        this.dataSyncService = dataSyncService;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void initializeScheduledTasks() {
        log.info("Initializing scheduled synchronization tasks...");
        refreshAllSchedules();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down scheduler service...");
        cancelAllScheduledTasks();
    }

    /**
     * Refreshes all scheduled tasks from the database.
     * This method cancels existing tasks and reschedules based on current database state.
     */
    public void refreshAllSchedules() {
        log.info("Refreshing all synchronization schedules...");

        cancelAllScheduledTasks();

        try {
            List<SyncConfiguration> activeConfigs = syncConfigurationRepository.findAllActiveWithSchedules();
            log.info("Found {} active configurations with schedules", activeConfigs.size());

            for (SyncConfiguration config : activeConfigs) {
                scheduleTask(config);
            }
        } catch (Exception e) {
            log.error("Failed to refresh schedules: {}", e.getMessage(), e);
        }
    }

    /**
     * Schedules or reschedules a specific sync configuration.
     */
    public void scheduleTask(SyncConfiguration config) {
        if (config.getSyncSchedule() == null || config.getSyncSchedule().getCronExpression() == null) {
            log.warn("Configuration '{}' (id: {}) has no schedule defined, skipping",
                    config.getName(), config.getId());
            return;
        }

        String cronExpression = config.getSyncSchedule().getCronExpression();

        // Cancel existing task if present
        cancelScheduledTask(config.getId());

        try {
            validateCronExpression(cronExpression);

            CronTrigger trigger = new CronTrigger(cronExpression);
            SyncConfigurationDto configDto = modelMapper.map(config, SyncConfigurationDto.class);

            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                    () -> executeSyncTask(configDto),
                    trigger
            );

            scheduledTasks.put(config.getId(), scheduledFuture);
            log.info("Scheduled sync task for configuration '{}' (id: {}) with cron: {}",
                    config.getName(), config.getId(), cronExpression);

        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression '{}' for configuration '{}' (id: {}): {}",
                    cronExpression, config.getName(), config.getId(), e.getMessage());
            throw new InvalidConfigurationException("cronExpression",
                    "Invalid cron expression: " + cronExpression);
        }
    }

    /**
     * Schedules or reschedules a task by configuration ID.
     */
    public void scheduleTaskById(Long configId) {
        syncConfigurationRepository.findById(configId)
                .ifPresentOrElse(
                        this::scheduleTask,
                        () -> log.warn("Cannot schedule task - configuration not found with id: {}", configId)
                );
    }

    /**
     * Cancels a scheduled task for a specific configuration.
     */
    public void cancelScheduledTask(Long configId) {
        ScheduledFuture<?> existingTask = scheduledTasks.remove(configId);
        if (existingTask != null) {
            existingTask.cancel(false);
            log.info("Cancelled scheduled task for configuration id: {}", configId);
        }
    }

    /**
     * Cancels all scheduled tasks.
     */
    public void cancelAllScheduledTasks() {
        log.info("Cancelling all {} scheduled tasks", scheduledTasks.size());
        scheduledTasks.forEach((id, future) -> future.cancel(false));
        scheduledTasks.clear();
    }

    /**
     * Gets the number of currently scheduled tasks.
     */
    public int getScheduledTaskCount() {
        return scheduledTasks.size();
    }

    /**
     * Checks if a specific configuration has a scheduled task.
     */
    public boolean isTaskScheduled(Long configId) {
        ScheduledFuture<?> task = scheduledTasks.get(configId);
        return task != null && !task.isCancelled() && !task.isDone();
    }

    private void executeSyncTask(SyncConfigurationDto config) {
        log.info("Executing scheduled sync for configuration: {} (id: {})",
                config.getName(), config.getId());
        try {
            dataSyncService.startSynchronization(config);
        } catch (Exception e) {
            log.error("Scheduled sync failed for configuration '{}' (id: {}): {}",
                    config.getName(), config.getId(), e.getMessage(), e);
        }
    }

    private void validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression cannot be empty");
        }

        // Spring's CronTrigger will validate the expression when created
        // This is just a basic sanity check
        String[] parts = cronExpression.trim().split("\\s+");
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                    "Cron expression must have at least 6 fields (second minute hour day month weekday)");
        }
    }
}
