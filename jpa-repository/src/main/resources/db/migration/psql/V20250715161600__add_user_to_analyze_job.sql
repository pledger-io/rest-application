alter table analyze_job add column user_id bigint;
alter table analyze_job add column failed boolean not null default false;

insert into analyze_job(id, year_month_found, completed, user_id)
select gen_random_uuid(), year_month_found, completed, user_account.id
from analyze_job, user_account;

delete from analyze_job where user_id is null;

alter table analyze_job alter column user_id set not null;

alter table analyze_job add constraint fk_analyze_job_user foreign key (user_id) references user_account(id);
