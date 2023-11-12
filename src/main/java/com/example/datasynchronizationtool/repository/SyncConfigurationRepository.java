package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.SyncConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SyncConfigurationRepository extends JpaRepository<SyncConfiguration, Long> {
}
