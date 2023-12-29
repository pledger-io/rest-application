create table setting
(
    id    bigint       not null auto_increment,
    name  varchar(255) not null,
    type  varchar(255) not null,
    "value" varchar(255) not null,

    constraint pk_setting primary key (id),
    constraint uq_setting unique (name)
);

insert into setting (id, name, type, "value")
values (null, 'RegistrationOpen', 'FLAG', '1'),
       (null, 'ImportOutdated', 'FLAG', '1'),
       (null, 'AnalysisBudgetMonths', 'NUMBER', '3');
