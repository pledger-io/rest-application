SET FOREIGN_KEY_CHECKS=0;  -- turn off foreign key checks

TRUNCATE TABLE user_account;
TRUNCATE TABLE user_account_token;
TRUNCATE TABLE account;
TRUNCATE TABLE transaction_journal;
TRUNCATE TABLE transaction_part;
TRUNCATE TABLE account_synonym;
TRUNCATE TABLE contract;
TRUNCATE TABLE saving_goal;
TRUNCATE TABLE budget_expense;
TRUNCATE TABLE budget;
TRUNCATE TABLE budget_period;

SET FOREIGN_KEY_CHECKS=1;  -- turn on foreign key checks