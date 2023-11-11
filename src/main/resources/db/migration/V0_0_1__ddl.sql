CREATE TABLE sync_configuration
(
    id            INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255),
    source_system VARCHAR(255) NOT NULL,
    target_system VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL
);

CREATE TABLE field_mapping
(
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    source_field          VARCHAR(255) NOT NULL,
    target_field          VARCHAR(255) NOT NULL,
    transformation        VARCHAR(255),
    sync_configuration_id INT,
    FOREIGN KEY (sync_configuration_id) REFERENCES sync_configuration (id)
);

CREATE TABLE sync_schedule
(
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    cron_expression       VARCHAR(255) NOT NULL,
    sync_configuration_id INT UNIQUE,
    FOREIGN KEY (sync_configuration_id) REFERENCES sync_configuration (id)
);

CREATE TABLE sync_log
(
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    timestamp             DATETIME    NOT NULL,
    status                VARCHAR(50) NOT NULL,
    message               VARCHAR(255),
    sync_configuration_id INT,
    FOREIGN KEY (sync_configuration_id) REFERENCES sync_configuration (id)
);
