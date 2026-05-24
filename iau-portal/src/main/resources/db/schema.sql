-- Schema for complaints table used in development
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
