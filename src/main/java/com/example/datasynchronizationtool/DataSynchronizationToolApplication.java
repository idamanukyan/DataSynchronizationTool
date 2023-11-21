package com.example.datasynchronizationtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DataSynchronizationToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSynchronizationToolApplication.class, args);
    }

}
