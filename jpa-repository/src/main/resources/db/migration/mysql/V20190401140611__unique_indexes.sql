/**
create fulltext index idx_account_name on account (name);
create fulltext index idx_transaction_journal_desc on transaction_journal (description);
create fulltext index idx_transaction_part_desc on transaction_part (description);
**/

create index idx_transaction_lookup on transaction_journal (user_id, t_date);
create index idx_transaction_part_amount on transaction_part (deleted desc, amount asc);
create index idx_budget_window on budget (b_from desc, b_until desc);
