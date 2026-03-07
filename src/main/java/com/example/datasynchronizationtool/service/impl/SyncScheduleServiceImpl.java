package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.exception.ResourceNotFoundException;
import com.example.datasynchronizationtool.model.SyncSchedule;
import com.example.datasynchronizationtool.repository.SyncScheduleRepository;
import com.example.datasynchronizationtool.scheduler.DynamicSchedulerService;
import com.example.datasynchronizationtool.service.ISyncScheduleService;
import com.example.datasynchronizationtool.service.dtos.SyncScheduleDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncScheduleServiceImpl implements ISyncScheduleService {

    private static final Logger log = LoggerFactory.getLogger(SyncScheduleServiceImpl.class);

    private final SyncScheduleRepository syncScheduleRepository;
    private final ModelMapper modelMapper;
    private final DynamicSchedulerService dynamicSchedulerService;

    @Autowired
    public SyncScheduleServiceImpl(
            SyncScheduleRepository syncScheduleRepository,
            ModelMapper modelMapper,
            @Lazy DynamicSchedulerService dynamicSchedulerService) {
        this.syncScheduleRepository = syncScheduleRepository;
        this.modelMapper = modelMapper;
        this.dynamicSchedulerService = dynamicSchedulerService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncScheduleDto> getAllSyncSchedules() {
        log.debug("Fetching all sync schedules");
        List<SyncScheduleDto> schedules = syncScheduleRepository.findAll().stream()
                .map(syncSchedule -> modelMapper.map(syncSchedule, SyncScheduleDto.class))
                .collect(Collectors.toList());
        log.info("Retrieved {} sync schedules", schedules.size());
        return schedules;
    }

    @Override
    @Transactional(readOnly = true)
    public SyncScheduleDto getSyncScheduleById(Long id) {
        log.debug("Fetching sync schedule with id: {}", id);
        return syncScheduleRepository.findById(id)
                .map(syncSchedule -> {
                    log.info("Found sync schedule with id: {} (cron: {})", id, syncSchedule.getCronExpression());
                    return modelMapper.map(syncSchedule, SyncScheduleDto.class);
                })
                .orElseThrow(() -> {
                    log.warn("Sync schedule not found with id: {}", id);
                    return new ResourceNotFoundException("SyncSchedule", "id", id);
                });
    }

    @Override
    @Transactional
    public SyncScheduleDto saveSyncSchedule(SyncScheduleDto syncScheduleDto) {
        log.debug("Saving sync schedule with cron expression: {}", syncScheduleDto.getCronExpression());
        SyncSchedule syncSchedule = modelMapper.map(syncScheduleDto, SyncSchedule.class);
        SyncSchedule saved = syncScheduleRepository.save(syncSchedule);
        log.info("Saved sync schedule with id: {} (cron: {})", saved.getId(), saved.getCronExpression());

        // Refresh the scheduler to pick up the new/updated schedule
        if (saved.getSyncConfiguration() != null) {
            log.debug("Refreshing scheduler for configuration id: {}", saved.getSyncConfiguration().getId());
            dynamicSchedulerService.scheduleTaskById(saved.getSyncConfiguration().getId());
        }

        return modelMapper.map(saved, SyncScheduleDto.class);
    }

    @Override
    @Transactional
    public void deleteSyncSchedule(Long id) {
        log.debug("Deleting sync schedule with id: {}", id);

        SyncSchedule schedule = syncScheduleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete - sync schedule not found with id: {}", id);
                    return new ResourceNotFoundException("SyncSchedule", "id", id);
                });

        // Cancel the scheduled task if it exists
        if (schedule.getSyncConfiguration() != null) {
            log.debug("Cancelling scheduled task for configuration id: {}",
                    schedule.getSyncConfiguration().getId());
            dynamicSchedulerService.cancelScheduledTask(schedule.getSyncConfiguration().getId());
        }

        syncScheduleRepository.deleteById(id);
        log.info("Deleted sync schedule with id: {}", id);
    }
}
