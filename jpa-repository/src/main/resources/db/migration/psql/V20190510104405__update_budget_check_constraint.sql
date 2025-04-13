ALTER TABLE budget DROP CONSTRAINT IF EXISTS ck_budget_frame;
ALTER TABLE budget ADD CONSTRAINT ck_budget_frame CHECK (b_until IS NULL OR b_from < b_until);
