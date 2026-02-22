-- Alter the transaction failure code
alter table transaction_journal add column tp_enum char(25);

update transaction_journal set tp_enum = 'FROM_TO_SAME' where failure_code = 0;
update transaction_journal set tp_enum = 'AMOUNT_NOT_NULL' where failure_code = 1;
update transaction_journal set tp_enum = 'POSSIBLE_DUPLICATE' where failure_code = 2;

alter table transaction_journal drop column failure_code;
alter table transaction_journal change `tp_enum` `failure_code` char(25);

-- Alter the transaction type
alter table transaction_journal add column tp_enum char(10);

update transaction_journal set tp_enum = 'CREDIT' where type = 0;
update transaction_journal set tp_enum = 'DEBIT' where type = 1;
update transaction_journal set tp_enum = 'TRANSFER' where type = 2;

alter table transaction_journal drop column type;
alter table transaction_journal change `tp_enum` `type` char(10);