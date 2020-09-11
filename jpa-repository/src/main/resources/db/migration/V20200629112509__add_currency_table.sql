create table currency
(
    id             bigint       not null auto_increment,
    name           varchar(255) not null,
    symbol         char(1)      not null,
    code           char(3)      not null,
    decimal_places int          not null default 2,

    enabled        bit          not null default true,
    archived       bit          not null default false,

    constraint pk_currency primary key (id),
    constraint uk_currency_code unique (code)
);

insert into currency (id, name, symbol, code, decimal_places, enabled, archived)
values (1, 'Euro', '€', 'EUR', default, default, default),
       (2, 'US Dollar', '$', 'USD', default, default, default),
       (3, 'British Pound', '£', 'GBP', default, default, default);
