ALTER TABLE transaction_journal
    ADD COLUMN contract_id BIGINT;

ALTER TABLE transaction_journal
    ADD CONSTRAINT fk_transaction_journal_contract FOREIGN KEY (contract_id) REFERENCES contract (id);
