-- Test data for SpendingInsightProviderJpaIT

-- Add spending insights for demo-user
INSERT INTO spending_insights (id, type, category, severity, score, detected_date, message, year_month, user_id)
VALUES
    (1, 'UNUSUAL_AMOUNT', 'Groceries', 'WARNING', 0.85, '2023-01-15', 'Unusual spending amount in Groceries', '2023-01', 1),
    (2, 'BUDGET_EXCEEDED', 'Entertainment', 'ALERT', 0.95, '2023-01-20', 'Budget exceeded in Entertainment', '2023-01', 1),
    (3, 'SPENDING_SPIKE', 'Dining', 'INFO', 0.65, '2023-02-10', 'Spending spike detected in Dining', '2023-02', 1),
    (4, 'UNUSUAL_FREQUENCY', 'Groceries', 'WARNING', 0.75, '2023-02-15', 'Unusual transaction frequency in Groceries', '2023-02', 1);

-- Add metadata for insights
INSERT INTO spending_insight_metadata (insight_id, metadata_key, metadata_value)
VALUES
    (1, 'amount', '150.50'),
    (1, 'expected', '75.25'),
    (2, 'budget', '200.00'),
    (2, 'actual', '250.00'),
    (3, 'previous', '100.00'),
    (3, 'current', '200.00'),
    (4, 'expected_count', '4'),
    (4, 'actual_count', '8');

-- Add spending insight for demo-user-not (to test user filtering)
INSERT INTO spending_insights (id, type, category, severity, score, detected_date, message, year_month, user_id)
VALUES
    (5, 'UNUSUAL_AMOUNT', 'Groceries', 'WARNING', 0.85, '2023-01-15', 'Unusual spending amount in Groceries', '2023-01', 2);
