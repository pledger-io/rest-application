alter table user_account_token add created datetime;
alter table user_account_token add description varchar(255);

update user_account_token set created = date('1950-01-01');

alter table user_account_token change created created datetime not null;
