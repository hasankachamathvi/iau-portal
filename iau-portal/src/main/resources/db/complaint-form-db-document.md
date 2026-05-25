# Complaint Form Database Document

This document describes the database tables used by the complaint form workflow in development.

## Tables

### complaints
Stores the complaint record itself.

- `id`: primary key
- `crn`: complaint reference number, unique
- `category`: complaint category
- `description`: complaint narrative
- `complaint_date`: date of the incident
- `location`: incident location
- `reported_before`: whether the matter was reported previously
- `escalated`: whether the complaint involves senior management or IAU members
- `ciaboc_escalated`: escalation flag for CIABOC routing
- `status`: complaint workflow status
- `created_at`: insert timestamp
- `updated_at`: last update timestamp

### reporter
Stores the reporter details for named submissions.

- `complaint_id`: foreign key to `complaints.id`
- `anonymous_flag`: true when the reporter selected anonymous
- `full_name`: reporter name
- `email`: reporter email
- `phone`: reporter phone
- `employee_id`: optional staff identifier

### subjects
Stores one subject record per complaint.

- `complaint_id`: foreign key to `complaints.id`
- `full_name`: subject name(s)
- `role`: subject role or designation
- `organization`: organization of the subject
- `relationship`: relationship to the reporter

### evidence
Stores uploaded evidence metadata.

- `complaint_id`: foreign key to `complaints.id`
- `file_name`: original file name
- `file_path`: encrypted file location on disk
- `file_type`: MIME type

### audit_logs
Stores audit trail entries.

- `event_type`: audit event type
- `complaint_crn`: complaint reference linked to the event
- `actor`: user or identity responsible for the event
- `details`: free-form audit payload
- `created_at`: event timestamp

## Notes

- The development profile loads `src/main/resources/db/schema.sql` directly.
- The complaint service writes to `complaints`, `reporter`, `subjects`, `evidence`, and `audit_logs`.
- Anonymous submissions leave reporter identity fields disabled in the form and should not persist contact values.
- The schema keeps foreign keys on `complaint_id` with cascade delete so related rows are removed with the complaint.
 
## Deployment / MySQL

- For MySQL production or staging, use the Flyway migrations in `src/main/resources/db/migration/`.
- A new migration `V4__create_complaint_tables.sql` creates the required `complaints`, `reporter`, `subjects`, and `evidence` tables; the existing `V1..V3` migrations handle `audit_logs` and column updates.
- A helper script `src/main/resources/db/init_mysql.sql` is provided to create the `iau_portal` database and an application user; replace `PASSWORD_PLACEHOLDER` before running.

Example minimal MySQL setup commands (run as a DBA user):

mysql -u root -p
SOURCE src/main/resources/db/init_mysql.sql;

After creating the DB, run Flyway (or start the application with Flyway enabled) to apply migrations.
