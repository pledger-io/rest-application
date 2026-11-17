insert into financial_health_snapshot
    (user_id, year_month_period, status, window_months, algorithm_version, currency, computed_at)
values
    (1, '2010-01', 'PROVISIONAL', 6, 1, 'EUR', '2010-01-31 12:00:00'),
    (1, '2010-02', 'PROVISIONAL', 12, 2, 'USD', '2010-02-28 12:00:00'),
    (2, '2010-01', 'PROVISIONAL', 12, 2, 'USD', '2010-01-31 12:00:00');
