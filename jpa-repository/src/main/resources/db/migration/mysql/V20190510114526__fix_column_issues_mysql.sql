alter table budget change b_from b_from timestamp null default null;
alter table budget change b_until b_until timestamp null default null;

alter table import change created created timestamp not null default current_timestamp;
alter table import change finished finished timestamp null default null;

alter table transaction_journal change created created timestamp not null default current_timestamp;
alter table transaction_journal change updated updated timestamp not null default current_timestamp;
alter table transaction_journal change deleted deleted timestamp null default null;
alter table transaction_journal change t_date t_date timestamp null default null;
alter table transaction_journal change interest_date interest_date timestamp null default null;
alter table transaction_journal change book_date book_date timestamp null default null;

alter table transaction_part change created created timestamp not null default current_timestamp;
alter table transaction_part change updated updated timestamp not null default current_timestamp;
alter table transaction_part change deleted deleted timestamp null default null;
