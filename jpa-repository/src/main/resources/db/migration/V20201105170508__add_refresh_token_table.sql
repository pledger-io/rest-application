create table user_account_token (
    id            bigint        not null auto_increment,
    refresh_token varchar(255)  unique not null,
    expires       date          not null,
    user_id       bigint        not null,

    constraint pk_user_account_token primary key (id),
    constraint fk_token_user_account foreign key (user_id) references user_account (id)
);
