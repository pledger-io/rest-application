insert into account(id, user_id, name, description, currency_id, archived, type_id, iban, bic, number)
values (1, 1, 'Account One', 'Demo Account', 1, false, 1, 'NLJND200001928233', '', ''),
       (2, 2, 'Account Two', 'Demo Account 2', 1, false, 1, 'NLJND200001928243', '', '');

insert into contract(id, name, start_date, end_date, company_id, user_id)
values (1, 'Test contract', '2019-02-01', '2020-02-01', 1, 1),
       (2, 'In between', '2019-02-01', '2020-02-01', 2, 2);
