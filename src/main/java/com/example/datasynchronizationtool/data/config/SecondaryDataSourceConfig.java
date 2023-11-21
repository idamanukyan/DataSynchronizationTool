package com.example.datasynchronizationtool.data.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class SecondaryDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "datasource.secondary")
    public DataSource secondaryDataSource() {
        return new DriverManagerDataSource();
    }
}