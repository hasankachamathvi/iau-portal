-- iau_portal.sql
-- Combined DB init + full schema + example queries for the IAU Complaint Portal
-- Intended for MySQL / MariaDB (adjust types/syntax for other RDBMS)

-- 1) Create database and application user (change PASSWORD_PLACEHOLDER)
CREATE DATABASE IF NOT EXISTS `iau_portal` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
USE `iau_portal`;

-- Create application user (change password before running)
CREATE USER IF NOT EXISTS 'iau_user'@'localhost' IDENTIFIED BY 'PASSWORD_PLACEHOLDER';
GRANT ALL PRIVILEGES ON `iau_portal`.* TO 'iau_user'@'localhost';
FLUSH PRIVILEGES;

-- 2) Full schema (tables, indexes, FKs)

CREATE TABLE IF NOT EXISTS `complaints` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `crn` VARCHAR(191) NOT NULL,
  `category` VARCHAR(255),
  `description` LONGTEXT,
  `complaint_date` DATE,
  `location` VARCHAR(255),
  `evidence_types` JSON DEFAULT NULL,
  `witness_names` VARCHAR(1000),
  `additional_info` LONGTEXT,
  `reported_before` TINYINT(1) DEFAULT 0,
  `escalated` TINYINT(1) DEFAULT 0,
  `ciaboc_escalated` TINYINT(1) DEFAULT 0,
  `declaration_confirmed` TINYINT(1) DEFAULT 0,
  `declaration_acknowledged` TINYINT(1) DEFAULT 0,
  `status` VARCHAR(50) DEFAULT 'PENDING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_complaints_crn` (`crn`),
  INDEX `idx_complaints_status` (`status`),
  INDEX `idx_complaints_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `reporter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `complaint_id` BIGINT NOT NULL,
  `anonymous_flag` TINYINT(1) DEFAULT 0,
  `full_name` VARCHAR(255),
  `email` VARCHAR(255),
  `phone` VARCHAR(50),
  `employee_id` VARCHAR(50),
  `division` VARCHAR(255),
  `designation` VARCHAR(255),
  PRIMARY KEY (`id`),
  INDEX `idx_reporter_complaint` (`complaint_id`),
  CONSTRAINT `fk_reporter_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `subjects` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `complaint_id` BIGINT NOT NULL,
  `full_name` VARCHAR(500),
  `role` VARCHAR(255),
  `organization` VARCHAR(255),
  `relationship` VARCHAR(255),
  PRIMARY KEY (`id`),
  INDEX `idx_subjects_complaint` (`complaint_id`),
  CONSTRAINT `fk_subject_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `evidence` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `complaint_id` BIGINT NOT NULL,
  `file_name` VARCHAR(500),
  `file_path` VARCHAR(1000),
  `file_type` VARCHAR(255),
  `file_size` BIGINT,
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_evidence_complaint` (`complaint_id`),
  CONSTRAINT `fk_evidence_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `event_type` VARCHAR(191) NOT NULL,
  `complaint_crn` VARCHAR(191),
  `actor` VARCHAR(191),
  `details` LONGTEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_audit_logs_event_type` (`event_type`),
  INDEX `idx_audit_logs_complaint_crn` (`complaint_crn`),
  INDEX `idx_audit_logs_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create reporter email index if it doesn't exist (portable across MySQL versions)
DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_idx_reporter_email$$
CREATE PROCEDURE ensure_idx_reporter_email()
BEGIN
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'reporter'
        AND INDEX_NAME = 'idx_reporter_email') = 0 THEN
    ALTER TABLE reporter ADD INDEX idx_reporter_email (email(128));
  END IF;
END$$
CALL ensure_idx_reporter_email()$$
DROP PROCEDURE IF EXISTS ensure_idx_reporter_email$$
DELIMITER ;


-- 3) Example queries

-- Insert a complaint (replace CRN generation as appropriate in application)
INSERT INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated)
VALUES ('CRN-2026-000123', 'fraud', 'Description of the incident...', '2026-05-20', 'Procurement Unit - Head Office', 0, 0);

-- Insert reporter (named submission)
INSERT INTO reporter (complaint_id, anonymous_flag, full_name, email, phone, employee_id, division, designation)
VALUES (LAST_INSERT_ID(), 0, 'Alice Reporter', 'alice@example.com', '+94123456789', 'EMP-1234', 'Procurement', 'Senior Buyer');

-- Insert a subject
INSERT INTO subjects (complaint_id, full_name, role, organization, relationship)
VALUES (LAST_INSERT_ID(), 'Bob Subject', 'Procurement Manager', 'SLT', 'Manager');

-- Insert evidence metadata
INSERT INTO evidence (complaint_id, file_name, file_path, file_type, file_size)
VALUES (LAST_INSERT_ID(), 'invoice.pdf', '/secure-storage/CRN-2026-000123/invoice.pdf', 'application/pdf', 245678);

-- Retrieve complaint with reporter (one query for primary data)
SELECT c.*, r.full_name AS reporter_name, r.email AS reporter_email
FROM complaints c
LEFT JOIN reporter r ON r.complaint_id = c.id
WHERE c.crn = 'CRN-2026-000123';

-- Retrieve subjects and evidence
SELECT s.* FROM subjects s WHERE s.complaint_id = (SELECT id FROM complaints WHERE crn = 'CRN-2026-000123');
SELECT e.* FROM evidence e WHERE e.complaint_id = (SELECT id FROM complaints WHERE crn = 'CRN-2026-000123');

-- Update status and add to audit log
UPDATE complaints SET status = 'IN_REVIEW', updated_at = CURRENT_TIMESTAMP WHERE crn = 'CRN-2026-000123';
INSERT INTO audit_logs (event_type, complaint_crn, actor, details)
VALUES ('UPDATE_STATUS', 'CRN-2026-000123', 'system:worker-1', 'Status changed to IN_REVIEW');

-- Pagination example: latest 25 complaints
SELECT c.id, c.crn, c.category, c.status, c.created_at
FROM complaints c
ORDER BY c.created_at DESC
LIMIT 0,25;

-- Delete a complaint (cascades to reporter/subjects/evidence)
DELETE FROM complaints WHERE crn = 'CRN-2026-000123';

-- End of iau_portal.sql
