create table tags
(
    id       bigint       not null auto_increment,
    name     varchar(255) not null,
    user_id  bigint       not null,
    archived bit          not null default false,

    constraint pk_tags primary key (id),
    constraint fk_tag_user foreign key (user_id) references user_account (id)
);

create table transaction_tag
(
    tag_id         bigint not null,
    transaction_id bigint not null,

    constraint pk_transaction_tag primary key (tag_id, transaction_id),
    constraint fk_transaction_tag foreign key (tag_id) references tags (id),
    constraint fk_tag_transaction foreign key (transaction_id) references transaction_journal (id)
);
