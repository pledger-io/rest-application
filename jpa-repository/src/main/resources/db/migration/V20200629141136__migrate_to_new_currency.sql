-- Change account currency to relation

alter table account
    add column currency_id bigint;

update account
set account.currency_id = (select id from currency where code = account.currency);

alter table account drop column currency;

alter table account
    add constraint fk_account_currency
        foreign key (currency_id) references currency (id);


-- Change transaction currency to relation

alter table transaction_journal
    add column currency_id bigint;

update transaction_journal
set transaction_journal.currency_id =
    (select id from currency where code = transaction_journal.currency);

alter table transaction_journal
    drop column currency;

alter table transaction_journal
    add constraint fk_transaction_currency
        foreign key (currency_id) references currency (id);
