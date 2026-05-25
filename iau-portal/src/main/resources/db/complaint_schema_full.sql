-- Full schema for Complaint Portal (MySQL)
-- Run this on a MySQL server (after creating the database)

CREATE TABLE IF NOT EXISTS `complaints` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `crn` VARCHAR(191) NOT NULL,
  `category` VARCHAR(255),
  `description` LONGTEXT,
  `complaint_date` DATE,
  `location` VARCHAR(255),
  `reported_before` TINYINT(1) DEFAULT 0,
  `escalated` TINYINT(1) DEFAULT 0,
  `ciaboc_escalated` TINYINT(1) DEFAULT 0,
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

-- Optional sample index for searching reporter by email (portable)
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
DROP PROCEDURE IF NOT EXISTS ensure_idx_reporter_email$$
DELIMITER ;
