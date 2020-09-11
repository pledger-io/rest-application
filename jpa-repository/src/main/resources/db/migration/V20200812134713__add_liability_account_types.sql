insert into account_type (label, hidden)
values ('loan', true),
       ('debt', true),
       ('mortgage', true);

alter table account
    add column interest decimal(7, 5) default 0;
alter table account
    add column interest_periodicity char(10);

update account set interest = 0;
