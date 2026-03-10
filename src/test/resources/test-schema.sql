-- Test schema for GenericDataRepository tests

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(255),
    email_address VARCHAR(255)
);
