alter table analyze_job add column user_id bigint;
alter table analyze_job add column failed bit not null default false;

insert into analyze_job(id, year_month_found, completed, user_id)
select uuid(), year_month_found, completed, user_account.id
from analyze_job, user_account;

delete from analyze_job where user_id is null;

alter table analyze_job change column user_id user_id bigint not null;

alter table analyze_job add constraint fk_analyze_job_user foreign key (user_id) references user_account(id);
