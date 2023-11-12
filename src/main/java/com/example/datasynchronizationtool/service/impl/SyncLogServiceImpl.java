package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncLog;
import com.example.datasynchronizationtool.repository.SyncLogRepository;
import com.example.datasynchronizationtool.service.ISyncLogService;
import com.example.datasynchronizationtool.service.dtos.SyncLogDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncLogServiceImpl implements ISyncLogService {

    private final SyncLogRepository syncLogRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SyncLogServiceImpl(SyncLogRepository syncLogRepository, ModelMapper modelMapper) {
        this.syncLogRepository = syncLogRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<SyncLogDto> getAllSyncLogs() {
        return syncLogRepository.findAll().stream()
                .map(syncLog -> modelMapper.map(syncLog, SyncLogDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SyncLogDto getSyncLogById(Long id) {
        return syncLogRepository.findById(id)
                .map(syncLog -> modelMapper.map(syncLog, SyncLogDto.class))
                .orElse(null);
    }

    @Override
    public SyncLogDto saveSyncLog(SyncLogDto syncLogDto) {
        SyncLog syncLog = modelMapper.map(syncLogDto, SyncLog.class);
        SyncLog saved = syncLogRepository.save(syncLog);
        return modelMapper.map(saved, SyncLogDto.class);
    }

    @Override
    public void deleteSyncLog(Long id) {
        syncLogRepository.deleteById(id);
    }
}
