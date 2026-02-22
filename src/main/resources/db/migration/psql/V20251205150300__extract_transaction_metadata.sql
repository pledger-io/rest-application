create table transaction_journal_meta
(
    id            BIGSERIAL not null,
    journal_id    bigint not null,
    entity_id     bigint not null,
    relation_type varchar(255),

    constraint transaction_journal_meta_pk primary key (id),
    constraint transaction_journal_meta_entity_id_fk foreign key (journal_id) references transaction_journal (id),
    constraint transaction_journal_meta_entity_id_uindex unique (journal_id, entity_id, relation_type)
);

insert into transaction_journal_meta(journal_id, relation_type, entity_id)
select
    id,
    'CATEGORY',
    category_id
from transaction_journal where category_id is not null;

insert into transaction_journal_meta(journal_id, relation_type, entity_id)
select
    id,
    'EXPENSE',
    budget_id
from transaction_journal where budget_id is not null;

insert into transaction_journal_meta(journal_id, relation_type, entity_id)
select
    id,
    'CONTRACT',
    contract_id
from transaction_journal where contract_id is not null;

insert into transaction_journal_meta(journal_id, relation_type, entity_id)
select
    transaction_id,
    'TAG',
    tag_id
from transaction_tag where tag_id is not null;

insert into transaction_journal_meta(journal_id, relation_type, entity_id)
select
    id,
    'IMPORT',
    batch_import_id
from transaction_journal where batch_import_id is not null;

alter table transaction_journal drop column budget_id;
alter table transaction_journal drop foreign key fk_transaction_journal_category;
alter table transaction_journal drop column category_id;
alter table transaction_journal drop foreign key fk_transaction_journal_contract;
alter table transaction_journal drop column contract_id;
alter table transaction_journal drop foreign key fk_transaction_journal_import;
alter table transaction_journal drop column batch_import_id;

