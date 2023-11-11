package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.SyncSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SyncScheduleRepository extends JpaRepository<SyncSchedule, Long> {
}
