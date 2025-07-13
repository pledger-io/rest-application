-- Test data for AnalyzeJobProviderJpaIT

-- Add analyze jobs for testing
INSERT INTO analyze_job (id, year_month_found, completed)
VALUES
    ('job-1', '2023-01', false),
    ('job-2', '2023-02', false),
    ('job-3', '2023-03', true);
