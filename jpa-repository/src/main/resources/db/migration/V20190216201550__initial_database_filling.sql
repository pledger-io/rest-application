insert into role(id, name) values
  (1, 'admin'),
  (2, 'accountant'),
  (3, 'reader');

insert into account_type (id, label, hidden) values
  (1, 'default', 0),
  (2, 'savings', 0),
  (3, 'joined', 0),
  (4, 'joined_savings', 0),
  (5, 'credit_card', 0),
  (6, 'cash', 0),
  (7, 'creditor', 1),
  (8, 'debtor', 1);
