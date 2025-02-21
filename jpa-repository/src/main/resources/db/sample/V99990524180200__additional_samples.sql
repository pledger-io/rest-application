INSERT INTO user_account (id, username, password, currency, gravatar, two_factor_enabled, two_factor_secret, theme)
VALUES (1, 'sample@e', '$2a$10$yZfinpG8MZtbjfKeNnrwlu4GMJuQLAV1.QnzcJPyrxjVIZMuPLYpi', null, null, false,
        'G5GABYRVECPIDLLG', 'dark');

insert into user_roles select 1, id from role;

insert into category (id, label, description, archived, user_id)
values (1, 'Groceries', 'Groceries', false, 1)
     , (2, 'Bus Fare', 'Transport', false, 1)
     , (3, 'Housing', 'Housing', false, 1)
     , (4, 'Utilities', 'Utilities', false, 1)
     , (5, 'Health', 'Health', false, 1)
     , (6, 'TV', 'TV', false, 1)
     , (7, 'Taxes', 'Taxes', false, 1)
     , (8, 'Salary', 'Salary', false, 1)
     , (9, 'Reconcile', 'Reconcile', false, 1)
     , (10, 'Other', 'Other', false, 1)
     , (11, 'Gas', 'Gas', false, 1)
     , (12, 'Car', 'Car', false, 1);

insert into tags (id, name, archived, user_id)
values (1, 'groceries', false, 1)
     , (2, 'transport', false, 1)
     , (3, 'housing', false, 1)
     , (4, 'utilities', false, 1)
     , (5, 'health', false, 1)
     , (6, 'entertainment', false, 1)
     , (7, 'taxes', false, 1)
     , (8, 'salary', false, 1)
     , (9, 'reconcile', false, 1)
     , (10, 'other', false, 1);

insert into budget (id, user_id, expected_income, b_from, b_until)
values (1, 1, 2500, '2018-01-01', null);
insert into budget_expense (id, name, user_id)
values (1, 'Groceries', 1)
     , (2, 'Fixed Expenses', 1)
     , (3, 'Transportation', 1)
     , (4, 'Insurance', 1)
     , (5, 'Entertainment', 1)
     , (6, 'Taxes', 1)
     , (7, 'Other', 1);
insert into budget_period (id, bp_lower_bound, bp_upper_bound, budget_id, expense_id)
values (1, 160, 200, 1, 1),
       (2, 1200, 1300, 1, 2),
       (3, 50, 150, 1, 3),
       (4, 350, 500, 1, 4),
       (5, 100, 150, 1, 5),
       (6, 200, 250, 1, 6),
       (7, 300, 400, 1, 7);


-- Setting up demo accounts for testing purposes
INSERT INTO account (id, user_id, name, description, iban, bic, number, archived, type_id, currency_id, interest,
                     interest_periodicity, image_file_token)
VALUES (100, 1, 'Personal checking', 'This is a sample account used for day to day payments.', 'NL23ABN0129388177263',
        null, null, false, 1, 1, 0.00000, null, null)
     , (101, 1, 'Personal savings', 'This is an active savings account for demo purposes.', null, null, 'XA0-32341123',
        false, 2, 1, 0.00000, null, null)
     , (102, 1, 'Archived savings account', 'This is an archived savings account', null, null, null, true, 2, 1,
        0.00000, null, null)
     , (103, 1, 'Visa card', 'Lorum ipsum dipsum.', null, null, null, false, 5, 1, 0.00000, null, null)
     , (104, 1, 'House mortgage', 'The mortgage on my home.', null, null, '938222-X102', false, 12, 1, 0.03500, 'YEARS',
        null)
     , (110, 1, 'Grocery store 1', 'Lorum ipsum dipsum.', 'NL03ING010928374615', null, null, false, 7, 1, 0.00000, null,
        null)
     , (111, 1, 'Car body shop', 'Lorum ipsum dipsum.', 'NL03ING0109234124615', null, null, false, 7, 1, 0.00000, null,
        null)
     , (112, 1, 'Taxation office', 'Lorum ipsum dipsum.', null, null, null, false, 7, 1, 0.00000, null, null)
     , (113, 1, 'Grocery store 2', 'Lorum ipsum dipsum.', 'NL03ING010928374615', null, null, false, 7, 1, 0.00000, null,
        null)
     , (114, 1, 'Insurance company', 'Lorum ipsum dipsum.', null, null, null, false, 7, 1, 0.00000, null, null)
     , (115, 1, 'Cable Company', 'Lorum ipsum dipsum.', null, null, null, false, 7, 1, 0.00000, null, null)
     , (106, 1, 'System Reconcile account', null, null, null, null, false, 9, 1, 0.00000, null, null)
     , (107, 1, 'Employer', null, null, null, null, false, 8, 1, 0.00000, null, null),
       (116, 1, 'Gas Station', null, null, null, null, false, 7, 1, 0.00000, null, null);

-- setup the mortgage initial balance
insert into transaction_journal(id, user_id, created, updated, t_date, deleted, description, category_id, budget_id,
                                type, currency_id)
values (1, 1, '2016-01-01', '2016-01-01', '2016-01-01', null, 'Opening balance', 3, null, 'CREDIT', 1);

insert into transaction_part(id, created, updated, journal_id, account_id, amount, description)
values (1, '2016-01-01', '2016-01-01', 1, 104, -323000, 'Opening balance'),
       (2, '2016-01-01', '2016-01-01', 1, 106, 323000, 'Opening balance');

-- create some random groceries transactions for 2016 until 2018, with multiple transactions spread over the month
CREATE
ALIAS InsertRandomTransactions FOR "db.migration.RandomizedTransactions.create";
CREATE
ALIAS InsertContract FOR "db.migration.RandomizedTransactions.createContract";

CALL InsertRandomTransactions(100, 110, 7, 10, 100, 'Grocery shopping', 'Groceries');
CALL InsertRandomTransactions(100, 113, 4, 70, 150, 'Happy shopping', 'Entertainment');
CALL InsertRandomTransactions(100, 111, 1, 2, 40, 'Car products', 'Other');
CALL InsertRandomTransactions(100, 112, 1, 150, 543, 'Local Taxes {month}', 'Taxes');
CALL InsertRandomTransactions(100, 104, 1, 670, 890, 'Mortgage payment {month}', 'Housing');
CALL InsertRandomTransactions(107, 100, 1, 2582, 2600, 'Salary {month}', 'Salary');
CALL InsertRandomTransactions(100, 114, 1, 250, 251, 'Health Insurance {month}', 'Health');
CALL InsertRandomTransactions(100, 114, 1, 50, 60, 'Car Insurance {month}', 'Car');
CALL InsertRandomTransactions(100, 115, 1, 75, 80, 'Cable subscription {month}', 'TV');
CALL InsertRandomTransactions(100, 101, 1, 10, 50, 'Saving for {month}', 'Other');
CALL InsertRandomTransactions(100, 116, 2, 45, 80, 'Fuel for car', 'Gas');

-- create a contract for the IRS
CALL InsertContract('IRS monthly payment', 112, '2018-01-01', '2020-06-01', 120);
CALL InsertContract('Cable Subscription', 115, '2016-01-01', '2050-06-01', 120);

update transaction_journal
set budget_id = 1
where exists (select 1
              from transaction_part
              where transaction_journal.id = transaction_part.journal_id
                and transaction_part.account_id = 110);
update transaction_journal
set budget_id = 2
where exists (select 1
              from transaction_part
              where transaction_journal.id = transaction_part.journal_id
                and transaction_part.account_id in (104,116));
update transaction_journal
set budget_id = 4
where exists (select 1
              from transaction_part
              where transaction_journal.id = transaction_part.journal_id
                and transaction_part.account_id in (114));
update transaction_journal
set budget_id = 5
where exists (select 1
              from transaction_part
              where transaction_journal.id = transaction_part.journal_id
                and transaction_part.account_id in (115));
update transaction_journal
set budget_id = 6
where exists (select 1
              from transaction_part
              where transaction_journal.id = transaction_part.journal_id
                and transaction_part.account_id in (112));

insert into rule_group(id, name, sort, user_id)
values (1,'Shopping classification', 0, 1),
       (2, 'Trading group', 1, 1);

insert into rule (id, name, restrictive, user_id, group_id, sort)
values (1, 'Grocery shopping', 0, 1, 1, 0),
       (2, 'Car shopping', 0, 1, 1, 1);

insert into rule_condition(cond_value, rule_id, field, operation)
values ('grocery', 1, 'DESCRIPTION', 'CONTAINS'),
       ('groceries', 1, 'DESCRIPTION', 'CONTAINS'),
       ('car', 2, 'DESCRIPTION', 'CONTAINS');

insert into rule_change(change_val, rule_id, field)
values ('1', 1, 'CATEGORY'),
       ('1', 1, 'BUDGET'),
       ('12', 2, 'CATEGORY');