insert into transaction_schedule (id, start_date, end_date, amount, name, periodicity, reoccur, user_id, source_id, destination_id)
values (1, '2018-01-01', '2019-01-01', 233, 'Expired schedule', 'MONTHS', 1, 1, 1, 2),
       (2, '2018-01-01', '2040-01-01', 15.22, 'Active schedule', 'MONTHS', 1, 1, 1, 2);

insert into contract(id, name, start_date, end_date, company_id, user_id)
values (1, 'Test contract', '2019-02-01', '2020-02-01', 2, 1);
