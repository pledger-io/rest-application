-- Test data for AnalyzeJobProviderJpaIT (all jobs completed)

-- Add only completed analyze jobs for testing
INSERT INTO analyze_job (id, year_month, completed)
VALUES
    ('job-1', '2023-01', true),
    ('job-2', '2023-02', true),
    ('job-3', '2023-03', true);
