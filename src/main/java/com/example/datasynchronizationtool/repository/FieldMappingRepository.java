package com.example.datasynchronizationtool.repository;

import com.example.datasynchronizationtool.model.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldMappingRepository extends JpaRepository<FieldMapping, Long> {
}
