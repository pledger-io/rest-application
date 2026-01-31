/**
create fulltext index idx_account_name on account (name);
create fulltext index idx_transaction_journal_desc on transaction_journal (description);
create fulltext index idx_transaction_part_desc on transaction_part (description);
**/

CREATE INDEX idx_transaction_lookup ON transaction_journal (user_id, t_date);
CREATE INDEX idx_transaction_part_amount ON transaction_part (deleted DESC, amount ASC);
CREATE INDEX idx_budget_window ON budget (b_from DESC, b_until DESC);
