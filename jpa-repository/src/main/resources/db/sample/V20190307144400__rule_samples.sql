insert into rule (id, name, user_id) values
  (1, 'Income locator', 1),
  (2, 'Groceries matcher', 1);

insert into rule_condition (id, field, operation, cond_value, rule_id) values
  (1, 3, 4, '2000', 1),
  (2, 3, 3, '5000', 1),
  (3, 2, 1, 'Salary', 1),
  (4, 2, 1, 'grocery', 2),
  (5, 3, 4, '20', 2),
  (6, 3, 3, '90', 2);

insert into rule_change (id, field, `value`, rule_id) values
  (1, 4, '1', 1),
  (2, 4, '2', 2),
  (3, 1, '2', 2);
