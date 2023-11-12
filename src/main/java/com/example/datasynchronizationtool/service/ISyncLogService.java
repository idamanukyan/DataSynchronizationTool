package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.model.SyncLog;

import java.util.List;
import java.util.Optional;

public interface ISyncLogService {
    List<SyncLog> getAllSyncLogs();

    Optional<SyncLog> getSyncLogById(Long id);

    SyncLog saveSyncLog(SyncLog syncLog);

    void deleteSyncLog(Long id);
}
