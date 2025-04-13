insert into account_type (id,label, hidden)
values (10, 'loan', true),
       (11, 'debt', true),
       (12, 'mortgage', true);

alter table account
    add column interest decimal(7, 5) default 0;
alter table account
    add column interest_periodicity char(10);

update account set interest = 0;
