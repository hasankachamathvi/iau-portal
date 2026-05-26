# IAU Complaint Portal - System Check (Submit → Track → Admin → DB)

## Goal
Verify that:
1) A complaint can be submitted.
2) Tracking by CRN returns the correct status.
3) Admin dashboard shows the submitted complaint.
4) Database contains the expected records (complaints + related tables + audit_logs).

## Prerequisites
- Database `iau_portal` exists and is reachable.
- Application is running (Spring Boot or equivalent).
- You can access these pages/endpoints:
  - Submit: `submit_response.html` (UI) → POST `/complaint/submit`
  - Track: `homepage.html` (UI) or template `/track` (server-side)
  - Admin: `/login` then `/admin/dashboard`

## Test Data
Use one of the existing JSON payloads (if your submit flow supports it):
- `complaint_DEV-0001.json`
- `complaint_IAU-2026-000002.json`

Otherwise, use the Submit UI and enter valid form fields (description length etc.).

## Test Workflow
### Step 1: Capture before state (DB)
Run these queries before submitting:

```sql
-- total complaints
SELECT COUNT(*) AS total FROM complaints;

-- status distribution
SELECT status, COUNT(*) AS cnt FROM complaints GROUP BY status;
```

### Step 2: Submit a new complaint
1. Open the Submit UI.
2. Fill in required fields.
3. Submit.
4. Copy the generated CRN shown on the confirmation page.

Expected backend behavior:
- A row is inserted into `complaints` with `crn`.
- Related rows inserted into `reporter` / `subjects` / `evidence` (depending on your submission).
- An entry is inserted into `audit_logs` with event_type `COMPLAINT_SUBMITTED` (and/or API variant).

### Step 3: Verify DB after submission
Replace `<CRN>` with the captured CRN.

```sql
-- 3.1 Complaint row exists
SELECT * FROM complaints WHERE crn = '<CRN>';

-- 3.2 Reporter row (if named submission)
SELECT * FROM reporter WHERE complaint_id = (SELECT id FROM complaints WHERE crn = '<CRN>');

-- 3.3 Subjects row
SELECT * FROM subjects WHERE complaint_id = (SELECT id FROM complaints WHERE crn = '<CRN>');

-- 3.4 Evidence row (if evidence uploaded)
SELECT * FROM evidence WHERE complaint_id = (SELECT id FROM complaints WHERE crn = '<CRN>');

-- 3.5 Audit log contains COMPLAINT_SUBMITTED
SELECT * FROM audit_logs
WHERE complaint_crn = '<CRN>'
ORDER BY created_at DESC
LIMIT 50;
```

Expected outcome:
- `complaints.status` should be `PENDING` right after submission.
- At least one `audit_logs` row for `<CRN>` with `event_type` containing `COMPLAINT_SUBMITTED`.

### Step 4: Track by CRN
1. Go to Track UI.
2. Enter `<CRN>`.
3. Submit.

Expected outcome:
- UI shows the same `crn` and `status` as DB (typically `PENDING`).

### Step 5: Admin view
1. Login as admin.
2. Open `/admin/dashboard`.
3. Filter by status if available (e.g., Pending).
4. Search for `<CRN>` (if admin UI has CRN search).
5. Open the complaint details page.

Expected outcome:
- The submitted complaint appears in the listing.
- The complaint details page loads without errors.

### Step 6: Status update (optional, but recommended)
If admin can update status:
1. Update `<CRN>` status to `UNDER_INVESTIGATION`.
2. Verify DB:
   - `complaints.status` changed.
   - `audit_logs` has `STATUS_UPDATED` (or equivalent event).
3. Re-check tracking UI.

## Common failure points
- CRN exists in UI but not in DB → submit handler not saving or transaction rolled back.
- Tracking shows “not found” → CRN mismatch/case sensitivity, or tracking endpoint/API differs.
- Admin doesn’t show complaint → admin listing query filters out some statuses or month/date range logic.
- Audit log missing → `auditLogService.record(...)` not called on submission path.

## Acceptance Criteria
- After submission, `<CRN>` exists in `complaints`.
- Tracking by `<CRN>` returns the same `status`.
- Admin dashboard listing includes `<CRN>` (at least under Pending/All).
- `audit_logs` includes a submission event for `<CRN>`.

