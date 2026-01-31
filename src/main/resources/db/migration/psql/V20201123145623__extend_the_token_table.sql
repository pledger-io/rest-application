alter table user_account_token add column created timestamp;
alter table user_account_token add column description varchar(255);

update user_account_token set created = '1950-01-01'::date;

alter table user_account_token alter column created type timestamp,
alter column created set not null;
