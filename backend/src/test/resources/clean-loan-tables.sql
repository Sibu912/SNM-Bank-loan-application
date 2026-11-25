-- Runs before tests that save data, so every test starts with empty loan tables.
-- (All test classes share the one snm_loans_test database.)
-- Children first, because of the foreign keys. Reviewer accounts are kept.
DELETE FROM application_status_history;
DELETE FROM loan_application;
DELETE FROM applicant;
