insert into user_account (id, username, password) values
  (1, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.');

insert into user_roles (users_id, roles_id) values
  (1, 1),
  (1, 2);

insert into category (id, label, description, archived, user_id) values
  (1, 'Salary', 'Income by salary', false, 1),
  (2, 'Groceries', 'Spending on groceries', false, 1),
  (3, 'Car', 'Spending on car', false, 1);

insert into account(id, user_id, name, currency, archived, type_id) values
  (1, 1, 'Demo checking account', 'EUR', false, 1),
  (2, 1, 'Groceries are us', 'EUR', false, 7),
  (3, 1, 'Boss & Co.', 'EUR', false, 8);
