package com.example.datasynchronizationtool.repository.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class SecondaryDataRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SecondaryDataRepository(@Qualifier("secondaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

}
