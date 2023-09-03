
insert into currency (id, name, symbol, code, decimal_places, enabled, archived)
values (1, 'Euro', '?', 'EUR', default, default, default),
       (2, 'US Dollar', '$', 'USD', default, default, default),
       (3, 'British Pound', '£', 'GBP', default, default, default);

insert into user_account (id, username, password, two_factor_secret) values
    (1, 'demo-user', '1234567', 'JBSWY3DPEHPK3PXP'),
    (2, 'demo-user-not', '1234567', 'JBSWY3DPEHPK3PXP');

insert into user_account_token (id, user_id, refresh_token, expires) values
    (1, 1, 'refresh-token-1', '2040-01-01')
