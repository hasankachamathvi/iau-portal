# Completion Status — IAU Portal Project

## Summary
All core functional features are now implemented and compiled. Evidence encryption, Flyway migrations, and project documentation are complete.

## Implemented Features

### 1. Portal Structure & Navigation ✅
- Multi-step complaint form with progress indicator
- Sequential navigation gating
- Autosave/resume via sessionStorage
- Confirmation page with CRN display and copy button

### 2. Reporter Information Section ✅
- Submission type (Named/Anonymous) toggle
- Reporter category dropdown
- Full name, email, phone, employee ID fields
- Client + server validation
- Fields hide when anonymous selected

### 3. Complaint Details Section ✅
- Category dropdown (14 categories)
- Date picker (prevents future dates)
- Location field
- Frequency dropdown
- Description textarea (50-char minimum)
- "How did you become aware?" options
- Conditional outcome field

### 4. Subject Information Section ✅
- Subject name, designation, organization
- Relationship dropdown
- Senior Management/IAU involvement question
- Auto-escalation to CIABOC based on subject/org matching
- High-risk flagging logic implemented

### 5. Evidence Upload Section ✅
- File upload component
- Supported types: PDF, DOCX, JPG, PNG, XLS, XLSX
- Validation: 10MB per file, max 5 files
- Server-side type/name validation
- **NEW: AES-256-GCM encryption at rest**
  - Files encrypted with 12-byte IV + GCM authentication tag
  - Decryption on download automatic
  - Key configured via `evidence.encryption.key` property

### 6. Declaration & Submission Section ✅
- Declaration checkboxes (3-4 statements)
- CAPTCHA (math question)
- Submit button gated until checks + CAPTCHA verified
- Server-side re-validation

### 7. CRN System ✅
- Automatic generation (IAU-YYYY-NNNNNN format)
- Uniqueness enforced via DB unique constraint
- Persisted to complaints table
- Displayed on confirmation page

### 8. Email Notification System ✅
- Confirmation email stub (logs when not configured)
- Spring Mail integration ready for SMTP config
- Email sent to reporter if not anonymous

### 9. Database Development ✅
- Schema tables: complaints, reporters, subjects, evidence, audit_log
- Relationships and timestamps (createdAt, updatedAt lifecycle methods)
- **NEW: Flyway migration automation enabled**
  - V1__add_audit_log_table.sql ✅
  - V2__add_ciaboc_escalated_column.sql ✅
  - V3__add_updated_at_column.sql ✅
- Automatic schema application on Spring Boot startup

### 10. Case Management System ✅
- Admin dashboard with filtering (all, pending, escalated, this month)
- Search by CRN
- Complaint detail view with reporter + evidence
- Status updates (PENDING, UNDER_INVESTIGATION, RESOLVED)
- CSV export (all filtered data)
- **NEW: PDF export with formatted table**
- **NEW: Audit-log viewer with event-type filter dropdown**
- Pagination (10 items/page, clamp-safe)
- Evidence download link with audit recording

### 11. Audit Logging ✅
- AuditLog entity + AuditLogService
- Audit event recording on:
  - Complaint submission
  - Admin complaint view
  - Status update
  - Export (CSV/PDF)
  - Evidence download
- Searchable via query string
- **NEW: Filterable by event type** (COMPLAINT_SUBMITTED, COMPLAINT_VIEWED, STATUS_UPDATED, EXPORT_CSV, EXPORT_PDF, EVIDENCE_DOWNLOADED, etc.)

### 12. Security Implementation
**Implemented:**
- ✅ Audit logging on all critical paths
- ✅ Anonymous submission handling (no identity capture for anonymous flagged)
- ✅ AES-256-GCM evidence encryption at rest
- ✅ Evidence download access gated to /admin (Spring Security)
- ✅ CSRF protection in place (Spring Security)

**Not Yet Implemented (production tasks):**
- HTTPS/TLS configuration (deployment / infrastructure)
- Production SMTP email config (operations)
- IP/device fingerprint prevention (logging config + analytics scrubbing)
- Role-based access control beyond basic admin (auth enhancement)
- Key rotation strategy (operations / secrets mgmt)

## New Files Created
- `src/main/java/com/slt/iau_portal/util/EncryptionUtil.java` — AES-256-GCM + helpers
- `src/main/resources/db/migration/V1__add_audit_log_table.sql`
- `src/main/resources/db/migration/V2__add_ciaboc_escalated_column.sql`
- `src/main/resources/db/migration/V3__add_updated_at_column.sql`
- `EVIDENCE_ENCRYPTION.md` — key generation & config guide
- `PROJECT_PLAN.md` — high-level architecture & timeline
- `README.md` — Flyway migration usage
- `COMPLETION_STATUS.md` (this file)

## Build Status
- ✅ Compiles cleanly (no errors)
- ⚠️ Tests blocked by MySQL unavailable (run with `-DskipTests`)

## To Deploy

1. **Configure environment:**
   - Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - Set `EVIDENCE_ENCRYPTION_KEY_BASE64` (generate via OpenSSL or script in EVIDENCE_ENCRYPTION.md)

2. **Start app:**
   ```bash
   java -jar target/iau-portal-0.0.1-SNAPSHOT.jar
   ```
   Flyway will automatically apply pending migrations.

3. **Access:**
   - Public: http://localhost:8080/complaint
   - Admin: http://localhost:8080/admin/dashboard (default admin/admin)

## Remaining Polish
- Production database migration validation (manual or CI)
- Integration tests with test MySQL
- Load testing on exports
- UI responsive design refinement
- Email template styling
- Role-based UI (reporters vs admins)

---
**Last Updated:** 18 May 2026 | **Compiled:** Yes | **Tests:** Skipped (DB unavailable)

