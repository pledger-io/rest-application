alter table budget
    modify b_from date not null;

alter table budget
    modify b_until date;
