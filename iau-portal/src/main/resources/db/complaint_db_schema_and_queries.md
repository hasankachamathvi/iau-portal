 # Complaint Database Schema & Example SQL Queries

 This document describes the database tables used by the Complaint Portal, the fields and types, and common SQL queries for inserting, retrieving and managing complaints.

 ## Tables Overview

 - `complaints`: main complaint record
 - `reporter`: reporter details (one-to-one with complaint)
 - `subjects`: subject(s) of the complaint (one-to-many)
 - `evidence`: uploaded evidence metadata (one-to-many)
 - `audit_logs`: audit trail for actions on complaints

 All example SQL below uses MySQL / MariaDB syntax (InnoDB, utf8mb4). Adjust types for other RDBMS.

 ---

 ## Table: `complaints`

 - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
 - `crn` VARCHAR(255) UNIQUE NOT NULL — complaint reference number
 - `category` VARCHAR(255)
 - `description` LONGTEXT
 - `complaint_date` DATE
 - `location` VARCHAR(255)
 - `reported_before` TINYINT(1) DEFAULT 0
 - `escalated` TINYINT(1) DEFAULT 0
 - `ciaboc_escalated` TINYINT(1) DEFAULT 0
 - `status` VARCHAR(50) DEFAULT 'PENDING'
 - `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
 - `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

 Purpose: store the complaint narrative and workflow flags.

 ---

 ## Table: `reporter`

 - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
 - `complaint_id` BIGINT NOT NULL REFERENCES `complaints`(`id`) ON DELETE CASCADE
 - `anonymous_flag` TINYINT(1) DEFAULT 0
 - `full_name` VARCHAR(255)
 - `email` VARCHAR(255)
 - `phone` VARCHAR(50)
 - `employee_id` VARCHAR(50)
 - `division` VARCHAR(255)
 - `designation` VARCHAR(255)

 Purpose: store named reporter details; if `anonymous_flag` = 1, personal fields should be null/empty.

 ---

 ## Table: `subjects`

 - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
 - `complaint_id` BIGINT NOT NULL REFERENCES `complaints`(`id`) ON DELETE CASCADE
 - `full_name` VARCHAR(500)
 - `role` VARCHAR(255)
 - `organization` VARCHAR(255)
 - `relationship` VARCHAR(255)

 Purpose: one row per named subject (multiple subjects allowed per complaint).

 ---

 ## Table: `evidence`

 - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
 - `complaint_id` BIGINT NOT NULL REFERENCES `complaints`(`id`) ON DELETE CASCADE
 - `file_name` VARCHAR(500)
 - `file_path` VARCHAR(1000)
 - `file_type` VARCHAR(255)
 - `file_size` BIGINT
 - `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP

 Purpose: metadata about uploaded files. Files themselves should be stored out-of-db (encrypted on disk or object store) and referenced by `file_path`.

 ---

 ## Table: `audit_logs`

 - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
 - `event_type` VARCHAR(255) NOT NULL
 - `complaint_crn` VARCHAR(255)
 - `actor` VARCHAR(255)
 - `details` LONGTEXT
 - `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP

 Purpose: record actions such as 'CREATE_COMPLAINT', 'UPDATE_STATUS', 'UPLOAD_EVIDENCE', etc.

 ---

 ## Full CREATE TABLE (MySQL) — example

 See `complaint_schema_full.sql` in the same folder for a ready-to-run SQL file. (It matches the definitions above.)

 ---

 ## Example SQL Usage

 -- 1) Insert a complaint (generate `crn` application-side; example uses UUID-like string)

 INSERT INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated)
 VALUES ('CRN-2026-000123', 'fraud', 'Description of the incident...', '2026-05-20', 'Procurement Unit - Head Office', 0, 0);

 -- 2) Insert reporter (named submission)

 INSERT INTO reporter (complaint_id, anonymous_flag, full_name, email, phone, employee_id, division, designation)
 VALUES (LAST_INSERT_ID(), 0, 'Alice Reporter', 'alice@example.com', '+94123456789', 'EMP-1234', 'Procurement', 'Senior Buyer');

 -- 3) Insert multiple subjects (example)

 INSERT INTO subjects (complaint_id, full_name, role, organization, relationship)
 VALUES
 (LAST_INSERT_ID(), 'Bob Subject', 'Procurement Manager', 'SLT', 'Manager');

 -- 4) Insert evidence metadata (application should store files and record path)

 INSERT INTO evidence (complaint_id, file_name, file_path, file_type, file_size)
 VALUES (LAST_INSERT_ID(), 'invoice.pdf', '/data/iau/evidence/CRN-2026-000123/invoice.pdf', 'application/pdf', 245678);

 -- 5) Retrieve a complaint with reporter, subjects and evidence

 SELECT c.*, r.full_name AS reporter_name, r.email AS reporter_email
 FROM complaints c
 LEFT JOIN reporter r ON r.complaint_id = c.id
 WHERE c.crn = 'CRN-2026-000123';

 -- To fetch subjects and evidence use additional joins or separate queries

 SELECT s.* FROM subjects s WHERE s.complaint_id = (SELECT id FROM complaints WHERE crn = 'CRN-2026-000123');
 SELECT e.* FROM evidence e WHERE e.complaint_id = (SELECT id FROM complaints WHERE crn = 'CRN-2026-000123');

 -- 6) Update complaint status and add audit log

 UPDATE complaints SET status = 'IN_REVIEW', updated_at = CURRENT_TIMESTAMP WHERE crn = 'CRN-2026-000123';

 INSERT INTO audit_logs (event_type, complaint_crn, actor, details)
 VALUES ('UPDATE_STATUS', 'CRN-2026-000123', 'system:worker-1', 'Status changed to IN_REVIEW');

 -- 7) Search recent complaints (pagination)

 SELECT c.id, c.crn, c.category, c.status, c.created_at
 FROM complaints c
 ORDER BY c.created_at DESC
 LIMIT 0, 25;

 -- 8) Delete a complaint (cascades to reporter/subjects/evidence)

 DELETE FROM complaints WHERE crn = 'CRN-2026-000123';

 ---

 Notes & best practices
 - Generate `crn` application-side using a stable pattern (YYYY + sequence or UUID) to avoid relying on auto-increment for external references.
 - Store files encrypted on disk or in object storage; keep only metadata and secure paths in the DB. Protect `file_path` access behind the application and ACLs.
 - Ensure all user-supplied strings are parameterised in SQL to prevent injection.
 - Add appropriate indexes for common searches (e.g., `created_at`, `status`, `crn`, `reporter.email`).
