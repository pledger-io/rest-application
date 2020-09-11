alter table transaction_journal
  add column budget_id bigint;

alter table transaction_journal
  add constraint fk_transaction_journal_budget foreign key (budget_id) references budget_expense (id);