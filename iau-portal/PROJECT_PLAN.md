Project Plan: IAU Complaint & Concern Reporting Portal

1. Overview
- Purpose: Secure intake and management of complaints with auditability and escalation.

2. Functional Requirements (summary)
- Multi-step complaint submission, anonymous option, CRN generation, evidence upload, admin dashboard, exports (CSV/PDF), audit logging, CIABOC escalation.

3. Non-functional Requirements
- Security: AES-256 encryption for evidence, TLS in transit, role-based access, audit trails.
- Reliability: DB migrations, backups, monitoring.
- Performance: Paginated admin lists, export limits.

4. Architecture (high level)
- Java 17 + Spring Boot (MVC + Spring Data JPA)
- MySQL for persistence
- Thymeleaf server-side UI
- File storage on disk (encrypted blobs)
- Audit logs table for event tracking
- Optional: external secrets manager for encryption key, S3 for evidence storage in production

5. Database Design (core tables)
- complaints (id, crn, category, description, complaint_date, location, escalated, ciaboc_escalated, status, created_at, updated_at)
- reporters (id, complaint_id, anonymous_flag, full_name, email, phone, employee_id)
- subjects (id, complaint_id, full_name, role, organization, relationship)
- evidence (id, complaint_id, file_name, file_path, file_type)
- audit_log (id, event_type, complaint_crn, actor, details, created_at)

6. Timeline (suggested)
- Week 1: Core form + persistence + CRN
- Week 2: Evidence upload + encryption + admin basic UI
- Week 3: Audit logging + CSV/PDF export + filters
- Week 4: Security hardening, tests, staging deploy
- Week 5: UAT, docs, production deploy

7. Testing Plan
- Unit tests: services (CRN uniqueness, encryption), controllers
- Integration tests: run against test MySQL (docker) for end-to-end flows
- Security tests: verify encryption at rest, RBAC, CSRF
- Performance: pagination, export load tests

8. Next steps
- Implement evidence encryption (done)
- Configure production key management
- Add CI to run integration tests with a test DB (docker-compose)

