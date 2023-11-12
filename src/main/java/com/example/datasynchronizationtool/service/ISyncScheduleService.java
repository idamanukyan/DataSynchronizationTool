package com.example.datasynchronizationtool.service;

import com.example.datasynchronizationtool.service.dtos.SyncScheduleDto;

import java.util.List;

public interface ISyncScheduleService {
    List<SyncScheduleDto> getAllSyncSchedules();

    SyncScheduleDto getSyncScheduleById(Long id);

    SyncScheduleDto saveSyncSchedule(SyncScheduleDto syncSchedule);

    void deleteSyncSchedule(Long id);

}
