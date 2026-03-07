package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.exception.ResourceNotFoundException;
import com.example.datasynchronizationtool.model.SyncLog;
import com.example.datasynchronizationtool.repository.SyncLogRepository;
import com.example.datasynchronizationtool.service.ISyncLogService;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncLogServiceImpl implements ISyncLogService {

    private static final Logger log = LoggerFactory.getLogger(SyncLogServiceImpl.class);

    private final SyncLogRepository syncLogRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SyncLogServiceImpl(SyncLogRepository syncLogRepository, ModelMapper modelMapper) {
        this.syncLogRepository = syncLogRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncLogDto> getAllSyncLogs() {
        log.debug("Fetching all sync logs");
        List<SyncLogDto> logs = syncLogRepository.findAll().stream()
                .map(syncLog -> modelMapper.map(syncLog, SyncLogDto.class))
                .collect(Collectors.toList());
        log.info("Retrieved {} sync logs", logs.size());
        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public SyncLogDto getSyncLogById(Long id) {
        log.debug("Fetching sync log with id: {}", id);
        return syncLogRepository.findById(id)
                .map(syncLog -> {
                    log.info("Found sync log with id: {} (status: {})", id, syncLog.getStatus());
                    return modelMapper.map(syncLog, SyncLogDto.class);
                })
                .orElseThrow(() -> {
                    log.warn("Sync log not found with id: {}", id);
                    return new ResourceNotFoundException("SyncLog", "id", id);
                });
    }

    @Override
    @Transactional
    public SyncLogDto saveSyncLog(SyncLogDto syncLogDto) {
        log.debug("Saving sync log with status: {}", syncLogDto.getStatus());
        SyncLog syncLog = modelMapper.map(syncLogDto, SyncLog.class);
        SyncLog saved = syncLogRepository.save(syncLog);
        log.info("Saved sync log with id: {} (status: {})", saved.getId(), saved.getStatus());
        return modelMapper.map(saved, SyncLogDto.class);
    }

    @Override
    @Transactional
    public void deleteSyncLog(Long id) {
        log.debug("Deleting sync log with id: {}", id);
        if (!syncLogRepository.existsById(id)) {
            log.warn("Cannot delete - sync log not found with id: {}", id);
            throw new ResourceNotFoundException("SyncLog", "id", id);
        }
        syncLogRepository.deleteById(id);
        log.info("Deleted sync log with id: {}", id);
    }
}
