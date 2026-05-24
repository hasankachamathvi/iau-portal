-- Idempotent sample complaints for development using MERGE (H2)
MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000001','Fraud','This is a test complaint description with enough length to satisfy validation rules.','2026-05-04',NULL,FALSE,FALSE,FALSE,'PENDING', TIMESTAMP '2026-05-04 14:53:27', NULL);

MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000002','Fraud','This is a follow-up test complaint description with additional context.','2026-05-04',NULL,FALSE,FALSE,FALSE,'PENDING', TIMESTAMP '2026-05-04 14:54:53', NULL);

MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000003','Fraud','kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk.','2022-06-06','office',FALSE,FALSE,FALSE,'PENDING', TIMESTAMP '2026-05-04 15:53:36', NULL);

MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000004','Fraud','A detailed complaint report describing suspected fraud in financial reporting.','2026-05-20','Colombo Head Office',FALSE,FALSE,FALSE,'PENDING', TIMESTAMP '2026-05-20 23:16:44', TIMESTAMP '2026-05-20 23:16:44.465511');

MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000005','Misconduct','Anonymous complaint about misconduct in the approval process; details omitted for brevity.','2026-05-20','Colombo Office',FALSE,FALSE,FALSE,'PENDING', TIMESTAMP '2026-05-20 23:26:13', TIMESTAMP '2026-05-20 23:26:13.153031');

MERGE INTO complaints (crn, category, description, complaint_date, location, reported_before, escalated, ciaboc_escalated, status, created_at, updated_at)
KEY(crn) VALUES ('IAU-2026-000006','Theft','qgydwhebdh deuyduiwewe akkskwkwn aaaaaaaaaaaaaaaaaaaaaaaa sample text to fill description.','2026-05-04','office',TRUE,TRUE,TRUE,'UNDER_INVESTIGATION', TIMESTAMP '2026-05-20 23:28:02', TIMESTAMP '2026-05-20 23:29:27.961675');
