SET FOREIGN_KEY_CHECKS=0;  -- turn off foreign key checks

TRUNCATE TABLE user_account;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE user_account_token;
TRUNCATE TABLE account;
TRUNCATE TABLE transaction_journal_meta;
TRUNCATE TABLE transaction_journal;
TRUNCATE TABLE transaction_part;
TRUNCATE TABLE account_synonym;
TRUNCATE TABLE contract;
TRUNCATE TABLE saving_goal;
TRUNCATE TABLE budget_expense;
TRUNCATE TABLE budget;
TRUNCATE TABLE budget_period;
TRUNCATE TABLE import_config;
TRUNCATE TABLE import;
TRUNCATE TABLE rule_group;
TRUNCATE TABLE rule;
TRUNCATE TABLE rule_change;
TRUNCATE TABLE rule_condition;
TRUNCATE TABLE transaction_schedule;
TRUNCATE TABLE tags;
TRUNCATE TABLE category;
TRUNCATE TABLE transaction_tag;
TRUNCATE TABLE currency;
TRUNCATE TABLE spending_insight_metadata;
TRUNCATE TABLE spending_insights;
TRUNCATE TABLE spending_pattern_metadata;
TRUNCATE TABLE spending_patterns;

SET FOREIGN_KEY_CHECKS=1;  -- turn on foreign key checks
