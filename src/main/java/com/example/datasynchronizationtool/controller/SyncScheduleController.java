package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.ISyncScheduleService;
import com.example.datasynchronizationtool.service.dtos.SyncScheduleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync-schedules")

public class SyncScheduleController {

    private final ISyncScheduleService syncScheduleService;

    @Autowired
    public SyncScheduleController(ISyncScheduleService syncScheduleService) {
        this.syncScheduleService = syncScheduleService;
    }

    @GetMapping
    public List<SyncScheduleDto> getAllSyncSchedules() {
        return syncScheduleService.getAllSyncSchedules();
    }

    @GetMapping("/{id}")
    public SyncScheduleDto getSyncScheduleById(@PathVariable Long id) {
        return syncScheduleService.getSyncScheduleById(id);
    }

    @PostMapping
    public SyncScheduleDto createSyncSchedule(@RequestBody SyncScheduleDto syncScheduleDto) {
        return syncScheduleService.saveSyncSchedule(syncScheduleDto);
    }

    @PutMapping("/{id}")
    public SyncScheduleDto updateSyncSchedule(
            @PathVariable Long id, @RequestBody SyncScheduleDto syncScheduleDto) {
        syncScheduleDto.setId(id);
        return syncScheduleService.saveSyncSchedule(syncScheduleDto);
    }

    @DeleteMapping("/{id}")
    public void deleteSyncSchedule(@PathVariable Long id) {
        syncScheduleService.deleteSyncSchedule(id);
    }
}
