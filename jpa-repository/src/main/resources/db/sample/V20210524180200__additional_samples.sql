INSERT INTO user_account (id, username, password, currency, gravatar, two_factor_enabled, two_factor_secret, theme)
VALUES (2, 'sample@e', '$2a$10$yZfinpG8MZtbjfKeNnrwlu4GMJuQLAV1.QnzcJPyrxjVIZMuPLYpi', null, null, false,
        'G5GABYRVECPIDLLG', 'dark');

-- Setting up demo accounts for testing purposes
INSERT INTO account (id, user_id, name, description, iban, bic, number, archived, type_id, currency_id, interest,
                     interest_periodicity, image_file_token)
VALUES (100, 2, 'Personal checking', 'This is a sample account used for day to day payments.', 'NL23ABN0129388177263', null, null, false, 1, 1, 0.00000, null, null)
     , (101, 2, 'Personal savings', 'This is an active savings account for demo purposes.', null, null, 'XA0-32341123', true, 2, 1, 0.00000, null, null)
     , (102, 2, 'Archived savings account', 'This is an archived savings account', null, null, null, false, 2, 1, 0.00000, null, null)
     , (103, 2, 'Visa card', 'Lorum ipsum dipsum.', null, null, null, false, 5, 1, 0.00000, null, null)
     , (104, 2, 'House mortgage', 'The mortgage on my home.', null, null, '938222-X102', false, 12, 1, 0.03500, 'YEARS', null)
     , (110, 2, 'Grocery store 1', 'Lorum ipsum dipsum.', 'NL03ING010928374615', null, null, false, 7, 1, 0.00000, null, null)
     , (111, 2, 'Car body shop', 'Lorum ipsum dipsum.', 'NL03ING0109234124615', null, null, false, 7, 1, 0.00000, null, null)
     , (111, 2, 'Taxation office', 'Lorum ipsum dipsum.', null, null, null, false, 7, 1, 0.00000, null, null)
     , (106, 2, 'System Reconcile account', null, null, null, null, false, 9, 1, 0.00000, null, null)
     , (107, 2, 'Employer', null, null, null, null, false, 8, 1, 0.00000, null, null);

insert into contract (id, name, start_date, end_date, company_id, archived, user_id)
values (100, 'IRS monthly payment', '2019-01-01', '2050-01-01', 111, false, 1);