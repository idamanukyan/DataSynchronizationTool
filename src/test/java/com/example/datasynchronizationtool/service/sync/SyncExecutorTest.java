package com.example.datasynchronizationtool.service.sync;

import com.example.datasynchronizationtool.exception.InvalidConfigurationException;
import com.example.datasynchronizationtool.exception.SyncFailedException;
import com.example.datasynchronizationtool.model.FieldMapping;
import com.example.datasynchronizationtool.repository.data.GenericDataRepository;
import com.example.datasynchronizationtool.service.dtos.SyncConfigurationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncExecutor Tests")
class SyncExecutorTest {

    @Mock
    private GenericDataRepository dataRepository;

    @Mock
    private DataSourceRegistry dataSourceRegistry;

    private TransformationEngine transformationEngine;
    private SyncExecutor syncExecutor;

    @BeforeEach
    void setUp() {
        transformationEngine = new TransformationEngine();
        syncExecutor = new SyncExecutor(dataRepository, transformationEngine, dataSourceRegistry);
    }

    private SyncConfigurationDto createValidConfig() {
        FieldMapping fieldMapping = FieldMapping.builder()
                .id(1L)
                .sourceField("users.name")
                .targetField("customers.full_name")
                .transformation("UPPERCASE")
                .isBiDirectional(false)
                .build();

        return SyncConfigurationDto.builder()
                .id(1L)
                .name("Test Sync")
                .sourceSystem("primary")
                .targetSystem("secondary")
                .isActive(true)
                .fieldMappings(Collections.singletonList(fieldMapping))
                .build();
    }

    @Nested
    @DisplayName("Configuration Validation")
    class ConfigurationValidation {

        @Test
        @DisplayName("Throws exception when source system is not configured")
        void missingSourceSystem() {
            SyncConfigurationDto config = createValidConfig();
            config.setSourceSystem(null);

            assertThrows(InvalidConfigurationException.class,
                    () -> syncExecutor.executeSynchronization(config));
        }

        @Test
        @DisplayName("Throws exception when target system is not configured")
        void missingTargetSystem() {
            SyncConfigurationDto config = createValidConfig();
            config.setTargetSystem(null);

            assertThrows(InvalidConfigurationException.class,
                    () -> syncExecutor.executeSynchronization(config));
        }

        @Test
        @DisplayName("Throws exception when field mappings are empty")
        void emptyFieldMappings() {
            SyncConfigurationDto config = createValidConfig();
            config.setFieldMappings(Collections.emptyList());

            assertThrows(InvalidConfigurationException.class,
                    () -> syncExecutor.executeSynchronization(config));
        }

        @Test
        @DisplayName("Throws exception when source datasource is not available")
        void sourceDataSourceNotAvailable() {
            SyncConfigurationDto config = createValidConfig();

            when(dataSourceRegistry.isDataSourceAvailable("primary")).thenReturn(false);

            assertThrows(InvalidConfigurationException.class,
                    () -> syncExecutor.executeSynchronization(config));
        }

        @Test
        @DisplayName("Throws exception when target datasource is not available")
        void targetDataSourceNotAvailable() {
            SyncConfigurationDto config = createValidConfig();

            when(dataSourceRegistry.isDataSourceAvailable("primary")).thenReturn(true);
            when(dataSourceRegistry.isDataSourceAvailable("secondary")).thenReturn(false);

            assertThrows(InvalidConfigurationException.class,
                    () -> syncExecutor.executeSynchronization(config));
        }
    }

    @Nested
    @DisplayName("Successful Synchronization")
    class SuccessfulSync {

        @Test
        @DisplayName("Synchronizes data with transformations applied")
        void syncWithTransformation() {
            SyncConfigurationDto config = createValidConfig();

            // Mock datasource availability
            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);

            // Mock source data
            List<Map<String, Object>> sourceData = new ArrayList<>();
            Map<String, Object> record1 = new HashMap<>();
            record1.put("id", 1L);
            record1.put("name", "john doe");
            sourceData.add(record1);

            when(dataRepository.readData(eq("primary"), eq("users"), anyList()))
                    .thenReturn(sourceData);

            // Mock upsert (return true for insert)
            when(dataRepository.upsertData(eq("secondary"), eq("customers"), anyMap(), anyString()))
                    .thenReturn(true);

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertNotNull(result);
            assertEquals(SyncResult.SyncStatus.SUCCESS, result.getStatus());
            assertEquals(1, result.getRecordsRead());
            assertEquals(1, result.getRecordsWritten());
            assertEquals(0, result.getRecordsFailed());

            // Verify transformation was applied
            verify(dataRepository).upsertData(
                    eq("secondary"),
                    eq("customers"),
                    argThat(map -> "JOHN DOE".equals(map.get("full_name"))),
                    anyString()
            );
        }

        @Test
        @DisplayName("Updates existing records correctly")
        void updateExistingRecords() {
            SyncConfigurationDto config = createValidConfig();

            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);

            List<Map<String, Object>> sourceData = new ArrayList<>();
            Map<String, Object> record = new HashMap<>();
            record.put("id", 1L);
            record.put("name", "jane doe");
            sourceData.add(record);

            when(dataRepository.readData(eq("primary"), eq("users"), anyList()))
                    .thenReturn(sourceData);

            // Return false for update (not insert)
            when(dataRepository.upsertData(eq("secondary"), eq("customers"), anyMap(), anyString()))
                    .thenReturn(false);

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertEquals(SyncResult.SyncStatus.SUCCESS, result.getStatus());
            assertEquals(1, result.getRecordsRead());
            assertEquals(0, result.getRecordsWritten());
            assertEquals(1, result.getRecordsUpdated());
        }

        @Test
        @DisplayName("Handles empty source data gracefully")
        void emptySourceData() {
            SyncConfigurationDto config = createValidConfig();

            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);
            when(dataRepository.readData(eq("primary"), eq("users"), anyList()))
                    .thenReturn(Collections.emptyList());

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertEquals(SyncResult.SyncStatus.NO_DATA, result.getStatus());
            assertEquals(0, result.getRecordsRead());
            assertTrue(result.getWarnings().size() > 0);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Handles read failure gracefully")
        void readFailure() {
            SyncConfigurationDto config = createValidConfig();

            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);
            when(dataRepository.readData(anyString(), anyString(), anyList()))
                    .thenThrow(new RuntimeException("Database connection failed"));

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertEquals(SyncResult.SyncStatus.FAILED, result.getStatus());
            assertTrue(result.getErrors().size() > 0);
        }

        @Test
        @DisplayName("Continues processing when individual record fails")
        void partialFailure() {
            FieldMapping fieldMapping = FieldMapping.builder()
                    .id(1L)
                    .sourceField("users.name")
                    .targetField("customers.full_name")
                    .build();

            SyncConfigurationDto config = SyncConfigurationDto.builder()
                    .id(1L)
                    .name("Test Sync")
                    .sourceSystem("primary")
                    .targetSystem("secondary")
                    .isActive(true)
                    .fieldMappings(Collections.singletonList(fieldMapping))
                    .build();

            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);

            List<Map<String, Object>> sourceData = new ArrayList<>();
            Map<String, Object> record1 = new HashMap<>();
            record1.put("id", 1L);
            record1.put("name", "john");
            sourceData.add(record1);

            Map<String, Object> record2 = new HashMap<>();
            record2.put("id", 2L);
            record2.put("name", "jane");
            sourceData.add(record2);

            when(dataRepository.readData(eq("primary"), eq("users"), anyList()))
                    .thenReturn(sourceData);

            // First call fails, second succeeds
            when(dataRepository.upsertData(eq("secondary"), eq("customers"), anyMap(), anyString()))
                    .thenThrow(new RuntimeException("Write failed"))
                    .thenReturn(true);

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertEquals(SyncResult.SyncStatus.PARTIAL_SUCCESS, result.getStatus());
            assertEquals(2, result.getRecordsRead());
            assertEquals(1, result.getRecordsWritten());
            assertEquals(1, result.getRecordsFailed());
        }
    }

    @Nested
    @DisplayName("Multiple Field Mappings")
    class MultipleFieldMappings {

        @Test
        @DisplayName("Processes multiple field mappings correctly")
        void multipleFieldMappings() {
            FieldMapping nameMapping = FieldMapping.builder()
                    .id(1L)
                    .sourceField("users.name")
                    .targetField("customers.full_name")
                    .transformation("UPPERCASE")
                    .build();

            FieldMapping emailMapping = FieldMapping.builder()
                    .id(2L)
                    .sourceField("users.email")
                    .targetField("customers.email_address")
                    .transformation("LOWERCASE")
                    .build();

            SyncConfigurationDto config = SyncConfigurationDto.builder()
                    .id(1L)
                    .name("Test Sync")
                    .sourceSystem("primary")
                    .targetSystem("secondary")
                    .isActive(true)
                    .fieldMappings(Arrays.asList(nameMapping, emailMapping))
                    .build();

            when(dataSourceRegistry.isDataSourceAvailable(anyString())).thenReturn(true);

            List<Map<String, Object>> sourceData = new ArrayList<>();
            Map<String, Object> record = new HashMap<>();
            record.put("id", 1L);
            record.put("name", "John Doe");
            record.put("email", "JOHN@EXAMPLE.COM");
            sourceData.add(record);

            when(dataRepository.readData(eq("primary"), eq("users"), anyList()))
                    .thenReturn(sourceData);
            when(dataRepository.upsertData(eq("secondary"), eq("customers"), anyMap(), anyString()))
                    .thenReturn(true);

            SyncResult result = syncExecutor.executeSynchronization(config);

            assertEquals(SyncResult.SyncStatus.SUCCESS, result.getStatus());
            assertEquals(2, result.getFieldsProcessed());

            verify(dataRepository).upsertData(
                    eq("secondary"),
                    eq("customers"),
                    argThat(map ->
                            "JOHN DOE".equals(map.get("full_name")) &&
                            "john@example.com".equals(map.get("email_address"))
                    ),
                    anyString()
            );
        }
    }
}
