create table saving_goal
(
    id           bigint         not null auto_increment,

    goal         decimal(22, 2) not null,
    allocated    decimal(22, 2) not null default 0,
    target_date  date           not null,

    name         varchar(250)   not null,
    description  varchar(1024),

    periodicity  char(10),
    reoccurrence decimal(10, 0),

    account_id   bigint         not null,

    constraint pk_saving_goal primary key (id),
    constraint fk_saving_goal_account foreign key (account_id) references account (id)
);