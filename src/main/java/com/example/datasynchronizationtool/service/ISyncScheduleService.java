package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.model.SyncSchedule;

import java.util.List;
import java.util.Optional;

public interface ISyncScheduleService {
    List<SyncSchedule> getAllSyncSchedules();

    Optional<SyncSchedule> getSyncScheduleById(Long id);

    SyncSchedule saveSyncSchedule(SyncSchedule syncSchedule);

    void deleteSyncSchedule(Long id);

}
