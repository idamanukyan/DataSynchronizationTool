package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.SyncSchedule;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SyncScheduleRepository extends JpaRepository<SyncSchedule, Long> {
}
