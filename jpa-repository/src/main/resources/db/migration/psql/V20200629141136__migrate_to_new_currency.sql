-- Change account currency to relation

ALTER TABLE account
    ADD COLUMN currency_id BIGINT;

UPDATE account
SET currency_id = (SELECT id FROM currency WHERE code = account.currency);

ALTER TABLE account DROP COLUMN currency;

ALTER TABLE account
    ADD CONSTRAINT fk_account_currency
        FOREIGN KEY (currency_id) REFERENCES currency (id);


-- Change transaction currency to relation

ALTER TABLE transaction_journal
    ADD COLUMN currency_id BIGINT;

UPDATE transaction_journal
SET currency_id = (SELECT id FROM currency WHERE code = transaction_journal.currency);

ALTER TABLE transaction_journal
DROP COLUMN currency;

ALTER TABLE transaction_journal
    ADD CONSTRAINT fk_transaction_currency
        FOREIGN KEY (currency_id) REFERENCES currency (id);
