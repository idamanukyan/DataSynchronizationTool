package com.example.datasynchronizationtool.service.impl;

import com.example.datasynchronizationtool.model.SyncSchedule;
import com.example.datasynchronizationtool.repository.SyncScheduleRepository;
import com.example.datasynchronizationtool.service.ISyncScheduleService;
import com.example.datasynchronizationtool.service.dtos.SyncScheduleDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncScheduleServiceImpl implements ISyncScheduleService {

    private final SyncScheduleRepository syncScheduleRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SyncScheduleServiceImpl(SyncScheduleRepository syncScheduleRepository, ModelMapper modelMapper) {
        this.syncScheduleRepository = syncScheduleRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<SyncScheduleDto> getAllSyncSchedules() {
        return syncScheduleRepository.findAll().stream()
                .map(syncSchedule -> modelMapper.map(syncSchedule, SyncScheduleDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SyncScheduleDto getSyncScheduleById(Long id) {
        return syncScheduleRepository.findById(id)
                .map(syncSchedule -> modelMapper.map(syncSchedule, SyncScheduleDto.class))
                .orElse(null);
    }

    @Override
    public SyncScheduleDto saveSyncSchedule(SyncScheduleDto syncScheduleDto) {
        SyncSchedule syncSchedule = modelMapper.map(syncScheduleDto, SyncSchedule.class);
        SyncSchedule saved = syncScheduleRepository.save(syncSchedule);
        return modelMapper.map(saved, SyncScheduleDto.class);
    }

    @Override
    public void deleteSyncSchedule(Long id) {
        syncScheduleRepository.deleteById(id);
    }
}
