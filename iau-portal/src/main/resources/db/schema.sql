-- Development schema for the complaint form workflow.

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crn VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    description CLOB,
    complaint_date DATE,
    location VARCHAR(255),
    reported_before BOOLEAN DEFAULT FALSE,
    escalated BOOLEAN DEFAULT FALSE,
    ciaboc_escalated BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reporter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    anonymous_flag BOOLEAN DEFAULT FALSE,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    employee_id VARCHAR(50),
    CONSTRAINT fk_reporter_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    full_name VARCHAR(500),
    role VARCHAR(255),
    organization VARCHAR(255),
    relationship VARCHAR(255),
    CONSTRAINT fk_subject_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    file_name VARCHAR(500),
    file_path VARCHAR(1000),
    file_type VARCHAR(255),
    CONSTRAINT fk_evidence_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(191) NOT NULL,
    complaint_crn VARCHAR(191),
    actor VARCHAR(191),
    details CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
