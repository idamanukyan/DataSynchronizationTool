package com.example.datasynchronizationtool.service.sync;

import com.example.datasynchronizationtool.repository.data.GenericDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Registry for managing data sources and registering them with the GenericDataRepository.
 * Registers the primary and secondary datasources on startup.
 */
@Component
public class DataSourceRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRegistry.class);

    public static final String PRIMARY_SYSTEM = "primary";
    public static final String SECONDARY_SYSTEM = "secondary";

    private final GenericDataRepository genericDataRepository;
    private final DataSource primaryDataSource;
    private final DataSource secondaryDataSource;

    @Autowired
    public DataSourceRegistry(
            GenericDataRepository genericDataRepository,
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.genericDataRepository = genericDataRepository;
        this.primaryDataSource = primaryDataSource;
        this.secondaryDataSource = secondaryDataSource;
    }

    @PostConstruct
    public void registerDataSources() {
        log.info("Registering data sources...");

        // Register the primary and secondary datasources with standard names
        genericDataRepository.registerDataSource(PRIMARY_SYSTEM, primaryDataSource);
        genericDataRepository.registerDataSource(SECONDARY_SYSTEM, secondaryDataSource);

        log.info("Data sources registered successfully");
    }

    /**
     * Register a custom datasource with a specific system name.
     * This allows dynamic registration of additional datasources at runtime.
     */
    public void registerCustomDataSource(String systemName, DataSource dataSource) {
        log.info("Registering custom datasource: {}", systemName);
        genericDataRepository.registerDataSource(systemName, dataSource);
    }

    /**
     * Check if a datasource is available for the given system name.
     */
    public boolean isDataSourceAvailable(String systemName) {
        return genericDataRepository.hasDataSource(systemName);
    }
}
