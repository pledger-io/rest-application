insert into budget_expense (id, name, user_id)
values (1, 'Groceries', 1),
       (2, 'Groceries', 2),
       (3, 'Savings', 1);

insert into budget (id, expected_income, user_id, b_from, b_until)
values (1, 2500, 1, '2019-01-01', '2019-02-01'),
       (2, 2800, 1, '2019-02-01', null),
       (3, 3000, 2, '2019-01-01', null);

insert into budget_period (id, bp_lower_bound, bp_upper_bound, expense_id, budget_id)
values (1, 200, 300, 1, 1),
       (2, 50, 150, 3, 1),
       (3, 250, 350, 1, 2),
       (4, 80, 200, 3, 2);

