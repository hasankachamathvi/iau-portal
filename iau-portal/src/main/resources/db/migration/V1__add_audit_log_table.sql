CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(100) NOT NULL,
    complaint_crn VARCHAR(100),
    actor VARCHAR(100),
    details TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_logs_event_type (event_type),
    INDEX idx_audit_logs_complaint_crn (complaint_crn),
    INDEX idx_audit_logs_actor (actor),
    INDEX idx_audit_logs_created_at (created_at)
);