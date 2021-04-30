alter table transaction_schedule
    add column contract_id bigint;

alter table transaction_schedule
    add constraint fk_schedule_contract foreign key (contract_id) references contract (id);
