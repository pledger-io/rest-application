create table contract
(
    id         bigint       not null auto_increment,
    name       varchar(255) not null,
    start_date timestamp    not null,
    end_date   timestamp    not null,
    company_id bigint,
    user_id    bigint       not null,
    file_token varchar(255),
    archived   bit(1)       not null default 0,

    constraint pk_contract primary key (id),
    constraint fk_contract_user foreign key (user_id) references user_account (id),
    constraint fk_contract_account foreign key (company_id) references account (id)
);
