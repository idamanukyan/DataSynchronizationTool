package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.scheduler.DynamicSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private static final Logger log = LoggerFactory.getLogger(SchedulerController.class);

    private final DynamicSchedulerService dynamicSchedulerService;

    @Autowired
    public SchedulerController(DynamicSchedulerService dynamicSchedulerService) {
        this.dynamicSchedulerService = dynamicSchedulerService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshSchedules() {
        log.info("Manual refresh of all schedules requested");
        dynamicSchedulerService.refreshAllSchedules();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Schedules refreshed successfully");
        response.put("activeSchedules", dynamicSchedulerService.getScheduledTaskCount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/configurations/{configId}/schedule")
    public ResponseEntity<Map<String, String>> scheduleConfiguration(@PathVariable Long configId) {
        log.info("Manual schedule request for configuration id: {}", configId);
        dynamicSchedulerService.scheduleTaskById(configId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Configuration scheduled successfully");
        response.put("configurationId", configId.toString());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/configurations/{configId}/schedule")
    public ResponseEntity<Map<String, String>> cancelScheduledTask(@PathVariable Long configId) {
        log.info("Cancel schedule request for configuration id: {}", configId);
        dynamicSchedulerService.cancelScheduledTask(configId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Scheduled task cancelled successfully");
        response.put("configurationId", configId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        log.debug("Scheduler status requested");

        Map<String, Object> response = new HashMap<>();
        response.put("activeSchedules", dynamicSchedulerService.getScheduledTaskCount());
        response.put("status", "running");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/configurations/{configId}/status")
    public ResponseEntity<Map<String, Object>> getConfigurationScheduleStatus(@PathVariable Long configId) {
        log.debug("Schedule status requested for configuration id: {}", configId);

        boolean isScheduled = dynamicSchedulerService.isTaskScheduled(configId);

        Map<String, Object> response = new HashMap<>();
        response.put("configurationId", configId);
        response.put("isScheduled", isScheduled);

        return ResponseEntity.ok(response);
    }
}
