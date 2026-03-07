package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.SyncConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SyncConfigurationRepository extends JpaRepository<SyncConfiguration, Long> {

    @Query("SELECT sc FROM SyncConfiguration sc " +
           "JOIN FETCH sc.syncSchedule ss " +
           "LEFT JOIN FETCH sc.fieldMappings " +
           "WHERE sc.isActive = true AND ss.cronExpression IS NOT NULL")
    List<SyncConfiguration> findAllActiveWithSchedules();

    List<SyncConfiguration> findByIsActiveTrue();
}
