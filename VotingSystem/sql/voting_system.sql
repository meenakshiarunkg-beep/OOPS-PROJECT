-- =====================================================================
--  ONLINE VOTING SYSTEM - DATABASE SCHEMA
--  Run this whole file once in MySQL Workbench / mysql CLI before
--  starting the Java application.
-- =====================================================================

DROP DATABASE IF EXISTS voting_system;
CREATE DATABASE voting_system;
USE voting_system;

-- ---------------------------------------------------------------------
-- Admin accounts
-- The application seeds a default admin (username: admin,
-- password: Admin@123) automatically the first time it runs,
-- so this table is intentionally left empty here.
-- ---------------------------------------------------------------------
CREATE TABLE admin (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt          VARCHAR(64)  NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Voters
-- ---------------------------------------------------------------------
CREATE TABLE voters (
    voter_id              INT AUTO_INCREMENT PRIMARY KEY,
    full_name             VARCHAR(100) NOT NULL,
    voter_code            VARCHAR(20)  UNIQUE NOT NULL, -- e.g. college ID / national voter ID
    email                 VARCHAR(100) UNIQUE NOT NULL,
    dob                   DATE NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    salt                  VARCHAR(64)  NOT NULL,
    security_question     VARCHAR(255) NOT NULL,
    security_answer_hash  VARCHAR(255) NOT NULL,
    security_answer_salt  VARCHAR(64)  NOT NULL,
    has_voted             BOOLEAN DEFAULT FALSE,
    is_locked             BOOLEAN DEFAULT FALSE,
    failed_attempts       INT DEFAULT 0,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Candidates
-- ---------------------------------------------------------------------
CREATE TABLE candidates (
    candidate_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    party          VARCHAR(100) NOT NULL,
    position_name  VARCHAR(100) NOT NULL DEFAULT 'General Seat',
    photo_path     VARCHAR(500),
    vote_count     INT DEFAULT 0,
    added_on       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Votes (audit trail - one row per vote cast)
-- ---------------------------------------------------------------------
CREATE TABLE votes (
    vote_id       INT AUTO_INCREMENT PRIMARY KEY,
    voter_id      INT NOT NULL,
    candidate_id  INT NOT NULL,
    voted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter_id)     REFERENCES voters(voter_id)         ON DELETE CASCADE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Single-row table holding overall election settings
-- ---------------------------------------------------------------------
CREATE TABLE election_settings (
    id              INT PRIMARY KEY DEFAULT 1,
    election_title  VARCHAR(200) NOT NULL DEFAULT 'Student Council Election 2026',
    voting_open     BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO election_settings (id, election_title, voting_open)
VALUES (1, 'Student Council Election 2026', TRUE);

-- ---------------------------------------------------------------------
-- Helpful indexes
-- ---------------------------------------------------------------------
CREATE INDEX idx_votes_candidate ON votes(candidate_id);
CREATE INDEX idx_voters_code ON voters(voter_code);
CREATE INDEX idx_voters_email ON voters(email);
