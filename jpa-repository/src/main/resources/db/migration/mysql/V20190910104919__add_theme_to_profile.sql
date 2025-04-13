alter table user_account add column theme varchar(255);

update user_account set theme = 'dark';
