package com.example.datasynchronizationtool.repository.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic repository for dynamic data access across different tables and fields.
 * Provides methods for reading and writing data based on configurable field mappings.
 */
@Repository
public class GenericDataRepository {

    private static final Logger log = LoggerFactory.getLogger(GenericDataRepository.class);

    private final Map<String, NamedParameterJdbcTemplate> jdbcTemplates;

    public GenericDataRepository() {
        this.jdbcTemplates = new HashMap<>();
    }

    /**
     * Register a datasource with a system name for later use.
     */
    public void registerDataSource(String systemName, DataSource dataSource) {
        log.info("Registering datasource for system: {}", systemName);
        this.jdbcTemplates.put(systemName.toLowerCase(), new NamedParameterJdbcTemplate(dataSource));
    }

    /**
     * Check if a datasource is registered for the given system.
     */
    public boolean hasDataSource(String systemName) {
        return jdbcTemplates.containsKey(systemName.toLowerCase());
    }

    /**
     * Read all records from a table, selecting specific fields.
     *
     * @param systemName The name of the system/datasource
     * @param tableName  The table to read from
     * @param fields     The fields to select
     * @return List of records as maps
     */
    public List<Map<String, Object>> readData(String systemName, String tableName, List<String> fields) {
        return readData(systemName, tableName, fields, null, null);
    }

    /**
     * Read records from a table with optional filtering.
     *
     * @param systemName   The name of the system/datasource
     * @param tableName    The table to read from
     * @param fields       The fields to select
     * @param whereClause  Optional WHERE clause (without the WHERE keyword)
     * @param params       Parameters for the WHERE clause
     * @return List of records as maps
     */
    public List<Map<String, Object>> readData(String systemName, String tableName, List<String> fields,
                                               String whereClause, Map<String, Object> params) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String fieldList = fields.stream()
                .map(this::escapeIdentifier)
                .collect(Collectors.joining(", "));

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(fieldList);
        sql.append(" FROM ").append(escapeIdentifier(tableName));

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        log.debug("Executing read query: {}", sql);

        MapSqlParameterSource sqlParams = new MapSqlParameterSource();
        if (params != null) {
            params.forEach(sqlParams::addValue);
        }

        try {
            return jdbc.queryForList(sql.toString(), sqlParams);
        } catch (DataAccessException e) {
            log.error("Failed to read data from {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Read a single field value from a table by primary key.
     */
    public Object readFieldValue(String systemName, String tableName, String fieldName,
                                  String primaryKeyField, Object primaryKeyValue) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String sql = String.format("SELECT %s FROM %s WHERE %s = :pkValue",
                escapeIdentifier(fieldName),
                escapeIdentifier(tableName),
                escapeIdentifier(primaryKeyField));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("pkValue", primaryKeyValue);

        log.debug("Executing field read: {}", sql);

        try {
            return jdbc.queryForObject(sql, params, Object.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            log.error("Failed to read field {} from {}.{}: {}", fieldName, systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Insert a new record into a table.
     *
     * @param systemName The name of the system/datasource
     * @param tableName  The table to insert into
     * @param data       Map of field names to values
     * @return Number of rows affected
     */
    public int insertData(String systemName, String tableName, Map<String, Object> data) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        List<String> fields = new ArrayList<>(data.keySet());
        String fieldList = fields.stream()
                .map(this::escapeIdentifier)
                .collect(Collectors.joining(", "));
        String valueList = fields.stream()
                .map(f -> ":" + f)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                escapeIdentifier(tableName), fieldList, valueList);

        log.debug("Executing insert: {}", sql);

        MapSqlParameterSource params = new MapSqlParameterSource();
        data.forEach(params::addValue);

        try {
            return jdbc.update(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to insert data into {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing record in a table.
     *
     * @param systemName      The name of the system/datasource
     * @param tableName       The table to update
     * @param data            Map of field names to new values
     * @param primaryKeyField The primary key field name
     * @param primaryKeyValue The primary key value to identify the record
     * @return Number of rows affected
     */
    public int updateData(String systemName, String tableName, Map<String, Object> data,
                          String primaryKeyField, Object primaryKeyValue) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String setClause = data.keySet().stream()
                .filter(f -> !f.equals(primaryKeyField))
                .map(f -> escapeIdentifier(f) + " = :" + f)
                .collect(Collectors.joining(", "));

        if (setClause.isEmpty()) {
            log.warn("No fields to update (excluding primary key)");
            return 0;
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s = :pkValue",
                escapeIdentifier(tableName), setClause, escapeIdentifier(primaryKeyField));

        log.debug("Executing update: {}", sql);

        MapSqlParameterSource params = new MapSqlParameterSource();
        data.forEach(params::addValue);
        params.addValue("pkValue", primaryKeyValue);

        try {
            return jdbc.update(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to update data in {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Upsert (insert or update) a record in a table.
     * First checks if the record exists, then inserts or updates accordingly.
     *
     * @param systemName      The name of the system/datasource
     * @param tableName       The table to upsert into
     * @param data            Map of field names to values
     * @param primaryKeyField The primary key field name
     * @return true if inserted, false if updated
     */
    public boolean upsertData(String systemName, String tableName, Map<String, Object> data,
                              String primaryKeyField) {
        Object primaryKeyValue = data.get(primaryKeyField);
        if (primaryKeyValue == null) {
            // No primary key value, do insert
            insertData(systemName, tableName, data);
            return true;
        }

        // Check if record exists
        if (recordExists(systemName, tableName, primaryKeyField, primaryKeyValue)) {
            updateData(systemName, tableName, data, primaryKeyField, primaryKeyValue);
            return false;
        } else {
            insertData(systemName, tableName, data);
            return true;
        }
    }

    /**
     * Check if a record exists in a table.
     */
    public boolean recordExists(String systemName, String tableName,
                                 String primaryKeyField, Object primaryKeyValue) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = :pkValue",
                escapeIdentifier(tableName), escapeIdentifier(primaryKeyField));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("pkValue", primaryKeyValue);

        try {
            Integer count = jdbc.queryForObject(sql, params, Integer.class);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("Failed to check record existence in {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a record from a table.
     */
    public int deleteData(String systemName, String tableName,
                          String primaryKeyField, Object primaryKeyValue) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String sql = String.format("DELETE FROM %s WHERE %s = :pkValue",
                escapeIdentifier(tableName), escapeIdentifier(primaryKeyField));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("pkValue", primaryKeyValue);

        log.debug("Executing delete: {}", sql);

        try {
            return jdbc.update(sql, params);
        } catch (DataAccessException e) {
            log.error("Failed to delete data from {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Get the count of records in a table.
     */
    public long getRecordCount(String systemName, String tableName) {
        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        String sql = String.format("SELECT COUNT(*) FROM %s", escapeIdentifier(tableName));

        try {
            Long count = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            log.error("Failed to get record count from {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Execute a batch insert for multiple records.
     */
    @SuppressWarnings("unchecked")
    public int[] batchInsert(String systemName, String tableName, List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            return new int[0];
        }

        NamedParameterJdbcTemplate jdbc = getJdbcTemplate(systemName);

        List<String> fields = new ArrayList<>(records.get(0).keySet());
        String fieldList = fields.stream()
                .map(this::escapeIdentifier)
                .collect(Collectors.joining(", "));
        String valueList = fields.stream()
                .map(f -> ":" + f)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                escapeIdentifier(tableName), fieldList, valueList);

        log.debug("Executing batch insert: {} records", records.size());

        MapSqlParameterSource[] batchParams = records.stream()
                .map(record -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    record.forEach(params::addValue);
                    return params;
                })
                .toArray(MapSqlParameterSource[]::new);

        try {
            return jdbc.batchUpdate(sql, batchParams);
        } catch (DataAccessException e) {
            log.error("Failed to batch insert into {}.{}: {}", systemName, tableName, e.getMessage());
            throw e;
        }
    }

    private NamedParameterJdbcTemplate getJdbcTemplate(String systemName) {
        NamedParameterJdbcTemplate jdbc = jdbcTemplates.get(systemName.toLowerCase());
        if (jdbc == null) {
            throw new IllegalArgumentException("No datasource registered for system: " + systemName);
        }
        return jdbc;
    }

    /**
     * Escape SQL identifier to prevent SQL injection.
     * Uses backticks for MySQL compatibility.
     */
    private String escapeIdentifier(String identifier) {
        // Remove any existing backticks and wrap in backticks
        return "`" + identifier.replace("`", "``") + "`";
    }
}
