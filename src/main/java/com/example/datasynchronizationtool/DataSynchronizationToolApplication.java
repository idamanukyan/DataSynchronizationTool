package com.example.datasynchronizationtool;

import com.example.datasynchronizationtool.repository.SyncConfigurationRepository;
import com.example.datasynchronizationtool.service.SyncConfigServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.example.datasynchronizationtool")
public class DataSynchronizationToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSynchronizationToolApplication.class, args);
    }

}
