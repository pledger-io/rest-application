alter table contract add column schedule_id bigint;
alter table contract add constraint fk_contract_schedule foreign key (schedule_id) references transaction_schedule(id);

update contract
  set schedule_id = (select id from transaction_schedule where contract_id = contract.id)
where exists (select 1 from transaction_schedule where contract_id = contract.id);

alter table transaction_schedule drop constraint fk_schedule_contract;
alter table transaction_schedule drop column contract_id;
