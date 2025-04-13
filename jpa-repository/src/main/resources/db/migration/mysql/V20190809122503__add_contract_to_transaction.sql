alter table transaction_journal
    add column contract_id bigint;

alter table transaction_journal
    add constraint fk_transaction_journal_contract foreign key (contract_id) references contract (id);
