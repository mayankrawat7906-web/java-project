-- Setup script for Environmental Reporting System
CREATE DATABASE IF NOT EXISTS environmental_system;
USE environmental_system;

-- Table for Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    points INT DEFAULT 0,
    is_blocked BOOLEAN DEFAULT FALSE,
    penalties INT DEFAULT 0
);

-- Table for Workers
CREATE TABLE IF NOT EXISTS workers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    assigned_count INT DEFAULT 0,
    resolved_count INT DEFAULT 0,
    complaint_count INT DEFAULT 0
);

-- Table for Reports
CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    latitude DOUBLE,
    longitude DOUBLE,
    description TEXT,
    reported_date VARCHAR(50),
    status VARCHAR(50),
    assigned_worker_id VARCHAR(50),
    category VARCHAR(50),
    upvotes INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (assigned_worker_id) REFERENCES workers(id)
);
