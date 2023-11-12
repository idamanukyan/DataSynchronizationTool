package com.example.datasynchronizationtool.controller;

import com.example.datasynchronizationtool.service.ISyncLogService;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync-logs")
public class SyncLogController {
    private final ISyncLogService syncLogService;

    @Autowired
    public SyncLogController(ISyncLogService syncLogService) {
        this.syncLogService = syncLogService;
    }

    @GetMapping
    public List<SyncLogDto> getAllSyncLogs() {
        return syncLogService.getAllSyncLogs();
    }

    @GetMapping("/{id}")
    public SyncLogDto getSyncLogById(@PathVariable Long id) {
        return syncLogService.getSyncLogById(id);
    }

    @PostMapping
    public SyncLogDto createSyncLog(@RequestBody SyncLogDto syncLogDto) {
        return syncLogService.saveSyncLog(syncLogDto);
    }

    @PutMapping("/{id}")
    public SyncLogDto updateSyncLog(@PathVariable Long id, @RequestBody SyncLogDto syncLogDto) {
        syncLogDto.setId(id);
        return syncLogService.saveSyncLog(syncLogDto);
    }

    @DeleteMapping("/{id}")
    public void deleteSyncLog(@PathVariable Long id) {
        syncLogService.deleteSyncLog(id);
    }
}
