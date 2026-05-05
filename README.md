# Data Synchronization Tool

> A Java tool that automates bi-directional data synchronization across heterogeneous sources, with customizable mapping, conflict resolution, scheduling, and audit logging.

## The problem

Enterprise systems rarely live in one database. CRMs, ERPs, data warehouses, and legacy systems all hold overlapping records — often with subtly different schemas and update frequencies. Manual reconciliation is slow, error-prone, and never catches up.

## What this does

A configurable synchronization engine that:

- **Connects to diverse data sources** — relational databases, REST APIs, and file-based exports
- **Maps fields between schemas** via declarative configuration
- **Resolves conflicts** with pluggable strategies — last-write-wins, source-of-truth precedence, or custom merge logic
- **Runs on a schedule** with retry-with-backoff and dead-letter handling for failed records
- **Logs every sync operation** for audit trails and debugging

## Stack

- **Java** with Spring Boot
- **Quartz** for scheduling
- **Jackson** for serialization
- **JDBC** drivers per source database
- **SLF4J + Logback** for structured logging

## Architecture

┌──────────┐     ┌──────────┐     ┌──────────┐
│ Source A │────►│ Mapping  │────►│ Conflict │
└──────────┘     │  Engine  │     │ Resolver │
└──────────┘     └────┬─────┘
┌──────────┐          ▲               │
│ Source B │──────────┘               ▼
└──────────┘                    ┌──────────┐
│  Target  │
└──────────┘
│
▼
┌──────────┐
│ Audit Log│
└──────────┘

Each sync job is defined declaratively. The engine reads from sources on a schedule, transforms records through a mapping layer, applies the configured conflict-resolution strategy, writes to the target, and emits a full audit record.

## Configuration

Sync jobs are defined in YAML:

```yaml
sync:
  name: customers-crm-to-warehouse
  schedule: "0 */15 * * * ?"      # every 15 minutes
  source:
    type: postgresql
    connection: ${CRM_DB_URL}
    table: customers
  target:
    type: postgresql
    connection: ${WAREHOUSE_DB_URL}
    table: dim_customers
  mapping:
    id: customer_id
    name: full_name
    email: email_address
    updated_at: last_modified
  conflict_resolution: last_write_wins
  retry:
    max_attempts: 3
    backoff_seconds: [5, 30, 120]
```

## Running

```bash
mvn clean package
java -jar target/data-sync-tool.jar --config=sync-config.yaml
```

## Status

Built for multi-source enterprise integration. Production-tested across 50+ data domains.

## License

MIT
