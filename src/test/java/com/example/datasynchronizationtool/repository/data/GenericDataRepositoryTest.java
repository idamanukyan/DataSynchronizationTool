package com.example.datasynchronizationtool.repository.data;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GenericDataRepository Tests")
class GenericDataRepositoryTest {

    private static final String TEST_SYSTEM = "test";
    private GenericDataRepository repository;
    private EmbeddedDatabase dataSource;

    @BeforeEach
    void setUp() {
        repository = new GenericDataRepository();

        // Use H2 with MySQL compatibility mode for ON DUPLICATE KEY UPDATE support
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb;MODE=MySQL")
                .addScript("classpath:test-schema.sql")
                .build();

        repository.registerDataSource(TEST_SYSTEM, dataSource);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null) {
            dataSource.shutdown();
        }
    }

    @Nested
    @DisplayName("DataSource Registration")
    class DataSourceRegistration {

        @Test
        @DisplayName("Registers datasource successfully")
        void registerDataSource() {
            assertTrue(repository.hasDataSource(TEST_SYSTEM));
        }

        @Test
        @DisplayName("Reports missing datasource correctly")
        void missingDataSource() {
            assertFalse(repository.hasDataSource("nonexistent"));
        }

        @Test
        @DisplayName("DataSource name is case insensitive")
        void caseInsensitive() {
            assertTrue(repository.hasDataSource("TEST"));
            assertTrue(repository.hasDataSource("Test"));
        }

        @Test
        @DisplayName("Throws exception when accessing unregistered datasource")
        void unregisteredDataSource() {
            assertThrows(IllegalArgumentException.class,
                    () -> repository.readData("unknown", "users", List.of("id")));
        }
    }

    @Nested
    @DisplayName("Insert Operations")
    class InsertOperations {

        @Test
        @DisplayName("Inserts single record successfully")
        void insertSingleRecord() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");

            int result = repository.insertData(TEST_SYSTEM, "users", data);

            assertEquals(1, result);
            assertEquals(1, repository.getRecordCount(TEST_SYSTEM, "users"));
        }

        @Test
        @DisplayName("Batch inserts multiple records")
        void batchInsert() {
            List<Map<String, Object>> records = new ArrayList<>();

            Map<String, Object> record1 = new LinkedHashMap<>();
            record1.put("id", 1L);
            record1.put("name", "John");
            record1.put("email", "john@example.com");
            records.add(record1);

            Map<String, Object> record2 = new LinkedHashMap<>();
            record2.put("id", 2L);
            record2.put("name", "Jane");
            record2.put("email", "jane@example.com");
            records.add(record2);

            int[] results = repository.batchInsert(TEST_SYSTEM, "users", records);

            assertEquals(2, results.length);
            assertEquals(2, repository.getRecordCount(TEST_SYSTEM, "users"));
        }

        @Test
        @DisplayName("Batch insert with empty list returns empty array")
        void batchInsertEmpty() {
            int[] results = repository.batchInsert(TEST_SYSTEM, "users", Collections.emptyList());

            assertEquals(0, results.length);
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {

        @BeforeEach
        void insertTestData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);

            data = new LinkedHashMap<>();
            data.put("id", 2L);
            data.put("name", "Jane Smith");
            data.put("email", "jane@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);
        }

        @Test
        @DisplayName("Reads all records with specified fields")
        void readAllRecords() {
            List<Map<String, Object>> results = repository.readData(
                    TEST_SYSTEM, "users", Arrays.asList("id", "name"));

            assertEquals(2, results.size());
            assertTrue(results.get(0).containsKey("id"));
            assertTrue(results.get(0).containsKey("name"));
        }

        @Test
        @DisplayName("Reads records with WHERE clause")
        void readWithWhereClause() {
            List<Map<String, Object>> results = repository.readData(
                    TEST_SYSTEM, "users", Arrays.asList("id", "name"),
                    "id = :id", Map.of("id", 1L));

            assertEquals(1, results.size());
            assertEquals("John Doe", results.get(0).get("name"));
        }

        @Test
        @DisplayName("Reads single field value by primary key")
        void readFieldValue() {
            Object value = repository.readFieldValue(
                    TEST_SYSTEM, "users", "name", "id", 1L);

            assertEquals("John Doe", value);
        }

        @Test
        @DisplayName("Returns null for non-existent record")
        void readNonExistentRecord() {
            Object value = repository.readFieldValue(
                    TEST_SYSTEM, "users", "name", "id", 999L);

            assertNull(value);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @BeforeEach
        void insertTestData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);
        }

        @Test
        @DisplayName("Updates existing record")
        void updateExistingRecord() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "John Updated");
            data.put("email", "john.updated@example.com");

            int result = repository.updateData(TEST_SYSTEM, "users", data, "id", 1L);

            assertEquals(1, result);

            Object name = repository.readFieldValue(TEST_SYSTEM, "users", "name", "id", 1L);
            assertEquals("John Updated", name);
        }

        @Test
        @DisplayName("Update returns zero for non-existent record")
        void updateNonExistent() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "Nobody");

            int result = repository.updateData(TEST_SYSTEM, "users", data, "id", 999L);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("Upsert Operations")
    class UpsertOperations {

        @Test
        @DisplayName("Inserts new record when not exists")
        void upsertInsert() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");

            boolean inserted = repository.upsertData(TEST_SYSTEM, "users", data, "id");

            assertTrue(inserted);
            assertEquals(1, repository.getRecordCount(TEST_SYSTEM, "users"));
        }

        @Test
        @DisplayName("Updates existing record")
        void upsertUpdate() {
            // First insert
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);

            // Then upsert with same ID
            Map<String, Object> updateData = new LinkedHashMap<>();
            updateData.put("id", 1L);
            updateData.put("name", "John Updated");
            updateData.put("email", "john.updated@example.com");

            boolean inserted = repository.upsertData(TEST_SYSTEM, "users", updateData, "id");

            assertFalse(inserted);
            assertEquals(1, repository.getRecordCount(TEST_SYSTEM, "users"));

            Object name = repository.readFieldValue(TEST_SYSTEM, "users", "name", "id", 1L);
            assertEquals("John Updated", name);
        }

        @Test
        @DisplayName("Upsert with null primary key attempts insert")
        void upsertWithNullPrimaryKey() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", null);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");

            // When PK is null, upsertData tries to insert
            // This may fail with constraint violation depending on DB schema
            // We just verify it returns true (insert path taken) or throws
            assertThrows(Exception.class, () ->
                repository.upsertData(TEST_SYSTEM, "users", data, "id"));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @BeforeEach
        void insertTestData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John Doe");
            data.put("email", "john@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);
        }

        @Test
        @DisplayName("Deletes existing record")
        void deleteExisting() {
            int result = repository.deleteData(TEST_SYSTEM, "users", "id", 1L);

            assertEquals(1, result);
            assertEquals(0, repository.getRecordCount(TEST_SYSTEM, "users"));
        }

        @Test
        @DisplayName("Delete returns zero for non-existent record")
        void deleteNonExistent() {
            int result = repository.deleteData(TEST_SYSTEM, "users", "id", 999L);

            assertEquals(0, result);
            assertEquals(1, repository.getRecordCount(TEST_SYSTEM, "users"));
        }
    }

    @Nested
    @DisplayName("Record Existence Check")
    class RecordExistence {

        @Test
        @DisplayName("Returns true for existing record")
        void existingRecord() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", 1L);
            data.put("name", "John");
            data.put("email", "john@example.com");
            repository.insertData(TEST_SYSTEM, "users", data);

            assertTrue(repository.recordExists(TEST_SYSTEM, "users", "id", 1L));
        }

        @Test
        @DisplayName("Returns false for non-existing record")
        void nonExistingRecord() {
            assertFalse(repository.recordExists(TEST_SYSTEM, "users", "id", 999L));
        }
    }

    @Nested
    @DisplayName("Record Count")
    class RecordCount {

        @Test
        @DisplayName("Returns zero for empty table")
        void emptyTable() {
            assertEquals(0, repository.getRecordCount(TEST_SYSTEM, "users"));
        }

        @Test
        @DisplayName("Returns correct count after inserts")
        void afterInserts() {
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("id", (long) i);
                data.put("name", "User " + i);
                data.put("email", "user" + i + "@example.com");
                repository.insertData(TEST_SYSTEM, "users", data);
            }

            assertEquals(5, repository.getRecordCount(TEST_SYSTEM, "users"));
        }
    }
}
