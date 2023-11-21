package com.example.datasynchronizationtool.repository.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class PrimaryDataRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrimaryDataRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

}
