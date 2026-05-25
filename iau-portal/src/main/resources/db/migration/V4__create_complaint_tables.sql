-- Flyway migration: create complaints and related tables for MySQL
-- Adjusts types for MySQL and creates necessary foreign keys

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
  UNIQUE KEY `uk_complaints_crn` (`crn`)
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
  PRIMARY KEY (`id`),
  INDEX `idx_evidence_complaint` (`complaint_id`),
  CONSTRAINT `fk_evidence_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
