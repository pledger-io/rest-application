insert into account(id, user_id, name, description, currency_id, archived, type_id, iban, bic, number)
values (1, 1, 'Account One', 'Demo Account', 1, false, 1, 'NLJND200001928233', '', ''),
       (2, 1, 'Account Two', 'Secondary', 1, false, 1, 'NLJND202001928233', '', ''),
       (3, 1, 'Debt', 'Secondary', 1, false, 10, '', '', '12312312');

insert into account_synonym(account_id, synonym)
values (2, 'Test account');
