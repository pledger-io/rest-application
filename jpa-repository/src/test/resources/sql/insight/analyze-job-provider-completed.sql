-- Test data for AnalyzeJobProviderJpaIT (all jobs completed)

-- Add only completed analyze jobs for testing
INSERT INTO analyze_job (id, year_month_found, user_id, completed)
VALUES ('job-1', '2023-01', 1, true),
       ('job-2', '2023-02', 1, true),
       ('job-3', '2023-03', 1, true);
