ALTER TABLE transaction_journal
    ADD COLUMN budget_id BIGINT;

ALTER TABLE transaction_journal
    ADD CONSTRAINT fk_transaction_journal_budget FOREIGN KEY (budget_id) REFERENCES budget_expense (id);
