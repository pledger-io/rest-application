alter table budget drop constraint ck_budget_frame;
alter table budget add constraint ck_budget_frame check (b_until = null or b_from < b_until);
