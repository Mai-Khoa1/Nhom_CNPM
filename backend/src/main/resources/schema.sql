-- Users table
CREATE TABLE IF NOT EXISTS users (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'ADMIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Horses table
CREATE TABLE IF NOT EXISTS horses (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL,
    breed VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    speed DOUBLE NOT NULL,
    owner VARCHAR(100),
    status VARCHAR(50) DEFAULT 'active',
    notes VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Jockeys table
CREATE TABLE IF NOT EXISTS jockeys (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL,
    country VARCHAR(100),
    wins INT DEFAULT 0,
    races INT DEFAULT 0,
    bio VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Schedules table
CREATE TABLE IF NOT EXISTS schedules (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(200) NOT NULL,
    race_date DATETIME NOT NULL,
    venue VARCHAR(100),
    horse_count INT DEFAULT 0,
    prize VARCHAR(50),
    status VARCHAR(50) DEFAULT 'upcoming',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Results table
CREATE TABLE IF NOT EXISTS results (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       race_name VARCHAR(200) NOT NULL,
    race_date DATETIME NOT NULL,
    venue VARCHAR(100),
    winner VARCHAR(100),
    jockey VARCHAR(100),
    time VARCHAR(50),
    prize VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_horses_name ON horses (name);
CREATE INDEX idx_jockeys_name ON jockeys (name);
CREATE INDEX idx_schedules_date ON schedules (race_date);
CREATE INDEX idx_schedules_status ON schedules (status);
CREATE INDEX idx_results_date ON results (race_date);