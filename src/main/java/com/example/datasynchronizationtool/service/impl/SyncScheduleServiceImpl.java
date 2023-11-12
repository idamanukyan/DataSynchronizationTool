package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncSchedule;
import com.example.datasynchronizationtool.repository.SyncScheduleRepository;
import com.example.datasynchronizationtool.service.ISyncScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SyncScheduleServiceImpl implements ISyncScheduleService {
    private final SyncScheduleRepository syncScheduleRepository;

    @Autowired
    public SyncScheduleServiceImpl(SyncScheduleRepository syncScheduleRepository) {
        this.syncScheduleRepository = syncScheduleRepository;
    }

    @Override
    public List<SyncSchedule> getAllSyncSchedules() {
        return syncScheduleRepository.findAll();
    }

    @Override
    public Optional<SyncSchedule> getSyncScheduleById(Long id) {
        return syncScheduleRepository.findById(id);
    }

    @Override
    public SyncSchedule saveSyncSchedule(SyncSchedule syncSchedule) {
        return syncScheduleRepository.save(syncSchedule);
    }

    @Override
    public void deleteSyncSchedule(Long id) {
        syncScheduleRepository.deleteById(id);
    }
}
