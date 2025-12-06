insert into account(id, user_id, name, description, currency_id, archived, type_id, iban, bic, number)
values (1, 1, 'Account One', 'Demo Account', 1, false, 1, 'NLJND200001928233', '', ''),
       (2, 1, 'Account Two', 'Demo Account 2', 1, false, 7, 'NLJND200001928243', '', ''),
       (3, 1, 'Split account 1', null, 1, false, 1, '', '', ''),
       (4, 1, 'Split account 2', null, 1, false, 7, '', '', '');

insert into category (id, label, user_id, archived)
values (1, 'Grocery', 1, false),
       (2, 'Test', 1, false);

insert into tags (id, name, user_id, archived)
values (1, 'Sample 1', 1, 0),
       (2, 'Food', 1, 0),
       (3, 'Drugs', 1, 0);

insert into transaction_journal (ID, created, updated, T_DATE, DESCRIPTION, currency_id, TYPE, USER_ID)
values (1, '2019-01-01', '2019-01-01', '2019-01-01', 'Sample transaction', 1, 'CREDIT', 1),
       (2, '2019-01-02', '2019-01-02', '2019-01-02', 'Split transaction sample', 1, 'CREDIT', 1),
       (3, '2019-01-01', '2019-01-01', '2019-01-01', 'Categorized transaction', 1, 'CREDIT', 1);

insert into transaction_journal_meta (journal_id, entity_id, relation_type)
values (3, 1, 'CATEGORY');

insert into transaction_part (ID, created, updated, deleted, JOURNAL_ID, ACCOUNT_ID, AMOUNT)
values (1, '2019-01-01', '2019-01-01', null, 1, 1, 20.2),
       (2, '2019-01-01', '2019-01-01', null, 1, 2, -20.2),
       (3, '2019-01-02', '2019-01-01', null, 2, 3, 20.2),
       (4, '2019-01-02', '2019-01-05', '2019-01-05', 2, 4, -20.2),
       (5, '2019-01-05', '2019-01-05', null, 2, 4, -10.2),
       (6, '2019-01-05', '2019-01-05', null, 2, 4, -10.0),
       (7, '2019-01-05', '2019-01-05', null, 3, 3, 10.0),
       (8, '2019-01-05', '2019-01-05', null, 3, 4, -10.0);

insert into transaction_tag (tag_id, transaction_id)
values (1, 1),
       (2, 1),
       (3, 2);
