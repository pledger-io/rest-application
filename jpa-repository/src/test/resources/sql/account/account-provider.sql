insert into account(id, user_id, name, description, currency_id, archived, type_id, iban, bic, number)
values (1, 1, 'Account One', 'Demo Account', 1, false, 1, 'NLJND200001928233', '', ''),
       (2, 2, 'Account Two', 'Demo Account 2', 1, false, 1, 'NLJND200001928243', '', '');

insert into transaction_journal (ID, created, updated, T_DATE, DESCRIPTION, CURRENCY_id, TYPE, USER_ID)
values (1, '2019-01-01', '2019-01-01', '2019-01-01', 'Sample transaction', 1, 0, 1);

insert into transaction_part (ID, created, updated, JOURNAL_ID, ACCOUNT_ID, AMOUNT)
values (1, '2019-01-01', '2019-01-01', 1, 1, 20.2),
       (2, '2019-01-01', '2019-01-01', 1, 2, -20.2);

insert into account_synonym (id, synonym, account_id)
values (1, 'Account sample', 1),
       (2, 'Account trial', 1),
       (3, 'Account Junk', 2);
