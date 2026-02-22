alter table budget
alter column b_from type date,
    alter column b_from set not null;

alter table budget
alter column b_until type date;
