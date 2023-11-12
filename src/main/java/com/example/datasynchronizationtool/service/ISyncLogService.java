package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.service.dtos.SyncLogDto;

import java.util.List;

public interface ISyncLogService {
    List<SyncLogDto> getAllSyncLogs();

    SyncLogDto getSyncLogById(Long id);

    SyncLogDto saveSyncLog(SyncLogDto syncLog);

    void deleteSyncLog(Long id);
}
