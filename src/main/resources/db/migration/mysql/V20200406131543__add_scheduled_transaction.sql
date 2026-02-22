create table transaction_schedule
(
    id             bigint         not null auto_increment,

    name           varchar(255),
    description    varchar(1024),
    amount         decimal(22, 2) not null,

    periodicity    char(10),
    reoccur        int,
    start_date     date,
    end_date       date,

    user_id        bigint         not null,
    source_id      bigint         not null,
    destination_id bigint         not null,

    constraint pk_transaction_schedule primary key (id),
    constraint fk_user foreign key (user_id) references user_account (id),
    constraint fk_source_account foreign key (source_id) references account (id),
    constraint fk_destination_account foreign key (destination_id) references account (id)
);