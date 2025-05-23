-- Tables with just their column definitions
create table account_type
(
    id     bigserial primary key,
    label  varchar(100) unique not null,
    hidden boolean not null
);

create table user_account
(
    id       bigserial primary key,
    username varchar(255) unique not null,
    password varchar(255) not null
);

create table role
(
    id   bigserial primary key,
    name varchar(255) unique not null
);

create table user_roles
(
    users_id bigint not null,
    roles_id bigint not null
);

create table account
(
    id          bigserial primary key,
    user_id     bigint not null,
    name        varchar(255) not null,
    description varchar(5000),
    currency    char(3) not null,
    iban        varchar(100),
    bic         varchar(50),
    number      varchar(100),
    archived    boolean not null,
    type_id     bigint not null
);

create table category
(
    id          bigserial primary key,
    label       varchar(255) not null,
    description varchar(1024),
    archived    boolean not null,
    user_id     bigint not null
);

create table import_config
(
    id       bigserial primary key,
    name     varchar(255) not null,
    contents text not null,
    user_id  bigint not null
);

create table import
(
    id        bigserial primary key,
    created   timestamp not null,
    finished  timestamp,
    slug      varchar(150),
    contents  text not null,
    config_id bigint not null,
    user_id   bigint not null
);

create table transaction_journal
(
    id              bigserial primary key,
    created         timestamp not null,
    updated         timestamp not null,
    deleted         timestamp,
    t_date          timestamp not null,
    interest_date   timestamp,
    book_date       timestamp,
    description     varchar(1024) not null,
    currency        char(3) not null,
    type            int not null,
    user_id         bigint not null,
    category_id     bigint,
    batch_import_id bigint
);

create table transaction_part
(
    id          bigserial primary key,
    created     timestamp not null,
    updated     timestamp not null,
    deleted     timestamp,
    journal_id  bigint not null,
    account_id  bigint not null,
    amount      decimal(22, 2) not null,
    description varchar(1024)
);

-- All constraints at the bottom of the file
alter table user_roles add constraint pk_user_role primary key (users_id, roles_id);
alter table user_roles add constraint fk_roles_users foreign key (users_id) references user_account (id);

alter table account add constraint fk_account_account_type foreign key (type_id) references account_type (id);
alter table account add constraint fk_account_user foreign key (user_id) references user_account (id);

alter table category add constraint fk_category_user foreign key (user_id) references user_account (id);

alter table import_config add constraint fk_import_config_user foreign key (user_id) references user_account (id);

alter table import add constraint uk_import_slug unique (slug);
alter table import add constraint fk_import_import_config foreign key (config_id) references import_config (id);
alter table import add constraint fk_import_user foreign key (user_id) references user_account (id);

alter table transaction_journal add constraint fk_transaction_journal_user foreign key (user_id) references user_account (id);
alter table transaction_journal add constraint fk_transaction_journal_category foreign key (category_id) references category (id);
alter table transaction_journal add constraint fk_transaction_journal_import foreign key (batch_import_id) references import (id);

alter table transaction_part add constraint fk_transaction_part_transaction_journal foreign key (journal_id) references transaction_journal (id);
alter table transaction_part add constraint fk_transaction_part_account foreign key (account_id) references account (id);
