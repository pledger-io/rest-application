create table financial_health_snapshot
(
    id                 BIGSERIAL      not null,
    user_id            bigint         not null,
    year_month_period  varchar(7)     not null,
    status             varchar(32)    not null,
    score              int,
    rating             varchar(32),
    window_months      int            not null,
    algorithm_version  int            not null,
    currency           varchar(3)     not null,
    savings_rate       decimal(9, 4),  savings_score  int,
    debt_to_income     decimal(9, 4),  dti_score      int,
    budget_adherence   decimal(9, 4),  budget_score   int,
    income             decimal(22, 2), expenses       decimal(22, 2), debt_payments decimal(22, 2),
    computed_at        timestamp      not null,

    constraint pk_financial_health_snapshot primary key (id),
    constraint uq_financial_health_user_period unique (user_id, year_month_period),
    constraint fk_financial_health_user foreign key (user_id) references user_account (id)
);
