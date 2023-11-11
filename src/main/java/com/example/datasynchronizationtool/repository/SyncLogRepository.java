package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
