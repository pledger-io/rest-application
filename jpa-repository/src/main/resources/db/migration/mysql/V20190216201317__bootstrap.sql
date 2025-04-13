create table account_type
(
    id     bigint              not null auto_increment,
    label  varchar(100) unique not null,
    hidden bit                 not null,
    constraint pk_account_type primary key (id)
);

create table user_account
(
    id       bigint              not null auto_increment,
    username varchar(255) unique not null,
    password varchar(255)        not null,
    constraint pk_user primary key (id)
);

create table role
(
    id   bigint              not null auto_increment,
    name varchar(255) unique not null,
    constraint pk_role primary key (id)
);

create table user_roles
(
    users_id bigint not null,
    roles_id bigint not null,
    constraint pk_user_role primary key (users_id, roles_id),
    constraint fk_roles_users foreign key (users_id) references user_account (id)
);

create table account
(
    id          bigint       not null auto_increment,
    user_id     bigint       not null,
    name        varchar(255) not null,
    description varchar(5000),
    currency    char(3)      not null,
    iban        varchar(100),
    bic         varchar(50),
    number      varchar(100),
    archived    bit          not null,

    type_id     bigint       not null,

    constraint pk_account primary key (id),
    constraint fk_account_account_type foreign key (type_id) references account_type (id),
    constraint fk_account_user foreign key (user_id) references user_account (id)
);

create table category
(
    id          bigint       not null auto_increment,

    label       varchar(255) not null,
    description varchar(1024),
    archived    bit          not null,

    user_id     bigint       not null,

    constraint pk_category primary key (id),
    constraint fk_category_user foreign key (user_id) references user_account (id)
);


create table import_config
(
    id       bigint       not null auto_increment,
    name     varchar(255) not null,
    contents text         not null,

    user_id  bigint       not null,

    constraint pk_import_config primary key (id),
    constraint fk_import_config_user foreign key (user_id) references user_account (id)
);

create table import
(
    id        bigint    not null auto_increment,
    created   timestamp not null,
    finished  timestamp,
    slug      varchar(150),
    contents  text      not null,

    config_id bigint    not null,
    user_id   bigint    not null,

    constraint pk_import primary key (id),
    constraint uk_import_slug unique (slug),
    constraint fk_import_import_config foreign key (config_id) references import_config (id),
    constraint fk_import_user foreign key (user_id) references user_account (id)
);

create table transaction_journal
(
    id              bigint        not null auto_increment,
    created         timestamp     not null,
    updated         timestamp     not null,
    deleted         timestamp,

    t_date          timestamp     not null,
    interest_date   timestamp,
    book_date       timestamp,
    description     varchar(1024) not null,
    currency        char(3)       not null,
    type            int           not null,

    user_id         bigint        not null,
    category_id     bigint,
    batch_import_id bigint,

    constraint pk_transaction_group primary key (id),
    constraint fk_transaction_journal_user foreign key (user_id) references user_account (id),
    constraint fk_transaction_journal_category foreign key (category_id) references category (id),
    constraint fk_transaction_journal_import foreign key (batch_import_id) references import (id)
);

create table transaction_part
(
    id          bigint          not null auto_increment,
    created     timestamp       not null,
    updated     timestamp       not null,
    deleted     timestamp,

    journal_id  bigint          not null,
    account_id  bigint          not null,
    amount      decimal(22, 2) not null,
    description varchar(1024),

    constraint pk_transaction_part primary key (id),
    constraint fk_transaction_part_transaction_journal foreign key (journal_id) references transaction_journal (id),
    constraint fk_transaction_part_account foreign key (account_id) references account (id)
);
