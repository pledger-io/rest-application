insert into role(id, name) values
  (1, 'admin'),
  (2, 'accountant'),
  (3, 'reader');

insert into account_type (id, label, hidden) values
  (1, 'default', false),
  (2, 'savings', false),
  (3, 'joined', false),
  (4, 'joined_savings', false),
  (5, 'credit_card', false),
  (6, 'cash', false),
  (7, 'creditor', true),
  (8, 'debtor', true);
