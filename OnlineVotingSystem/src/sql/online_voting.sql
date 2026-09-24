-- ============================================
-- Online Voting System - Database Setup Script
-- Run this ONCE in your MySQL client (phpMyAdmin,
-- MySQL Workbench, or the mysql command line) before
-- running the Java program.
-- ============================================

CREATE DATABASE IF NOT EXISTS online_voting;
USE online_voting;

-- Everyone who can vote. approved = FALSE until an admin approves them.
-- has_voted = FALSE until they cast their one vote.
CREATE TABLE IF NOT EXISTS voters (
    voter_id   INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    approved   BOOLEAN DEFAULT FALSE,
    has_voted  BOOLEAN DEFAULT FALSE
);

-- People running for election.
CREATE TABLE IF NOT EXISTS candidates (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    party        VARCHAR(100)
);

-- One row per accepted vote. voter_id/candidate_id link back to the tables above.
CREATE TABLE IF NOT EXISTS votes (
    vote_id      INT AUTO_INCREMENT PRIMARY KEY,
    voter_id     INT NOT NULL,
    candidate_id INT NOT NULL,
    FOREIGN KEY (voter_id) REFERENCES voters(voter_id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

-- Admin accounts. Kept separate from voters on purpose - an admin is not a voter.
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Default admin login: username "admin", password "admin123"
-- Change this before any real use - it's plaintext for prototype simplicity only.
INSERT INTO admins (username, password)
    VALUES ('admin', 'admin123')
    ON DUPLICATE KEY UPDATE username = username;

-- A couple of sample candidates so you have something to vote for immediately.
INSERT INTO candidates (name, party) VALUES ('Alice Smith', 'Green Party');
INSERT INTO candidates (name, party) VALUES ('Bob Jones', 'Blue Party');
