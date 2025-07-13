alter table spending_insights
    change `year_month` year_month_found char(7);
alter table spending_patterns
    change `year_month` year_month_found char(7);
alter table analyze_job
    change `year_month` year_month_found char(7);
