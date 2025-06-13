create table analyze_job
(
    id         varchar(255) not null,
    year_month varchar(7),
    completed  boolean default false,

    constraint pk_analyze_job primary key (id)
);
