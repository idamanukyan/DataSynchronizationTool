package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncLog;
import com.example.datasynchronizationtool.repository.SyncLogRepository;
import com.example.datasynchronizationtool.service.ISyncLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SyncLogServiceImpl implements ISyncLogService {

    private final SyncLogRepository syncLogRepository;

    @Autowired
    public SyncLogServiceImpl(SyncLogRepository syncLogRepository) {
        this.syncLogRepository = syncLogRepository;
    }

    @Override
    public List<SyncLog> getAllSyncLogs() {
        return syncLogRepository.findAll();
    }

    @Override
    public Optional<SyncLog> getSyncLogById(Long id) {
        return syncLogRepository.findById(id);
    }

    @Override
    public SyncLog saveSyncLog(SyncLog syncLog) {
        return syncLogRepository.save(syncLog);
    }

    @Override
    public void deleteSyncLog(Long id) {
        syncLogRepository.deleteById(id);
    }
}
