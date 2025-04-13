alter table user_account
    add column two_factor_enabled bit not null default false;

alter table user_account
    add column two_factor_secret char(32) not null default '';
