create table account_synonym (
    id bigint not null auto_increment,
    account_id bigint not null,
    synonym varchar(255) not null,

    constraint pk_account_synonym primary key (id),
    constraint fk_account_synonym_account foreign key (account_id) references account(id),
    constraint uq_synonym unique (synonym)
);
