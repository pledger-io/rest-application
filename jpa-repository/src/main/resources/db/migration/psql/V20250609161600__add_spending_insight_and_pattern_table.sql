-- Migration script for adding SpendingInsightJpa and SpendingPatternJpa tables
-- Filename: V20240601120000__add_spending_insight_and_pattern_tables.sql

-- Create spending_insights table
create table spending_insights
(
    id            bigint      not null auto_increment,
    type          varchar(50) not null,
    category      varchar(255),
    severity      varchar(50),
    score double not null,
    detected_date date,
    message       varchar(1024),
    year_month    varchar(7),
    transaction_id bigint,
    user_id       bigint      not null,

    constraint pk_spending_insights primary key (id),
    constraint fk_spending_insights_user foreign key (user_id) references user_account (id)
);

-- Create spending_insight_metadata table
create table spending_insight_metadata
(
    insight_id     bigint       not null,
    metadata_key   varchar(255) not null,
    metadata_value varchar(255),

    constraint pk_spending_insight_metadata primary key (insight_id, metadata_key),
    constraint fk_spending_insight_metadata foreign key (insight_id) references spending_insights (id)
);

-- Create spending_patterns table
create table spending_patterns
(
    id            bigint      not null auto_increment,
    type          varchar(50) not null,
    category      varchar(255),
    confidence double not null,
    detected_date date,
    year_month    varchar(7),
    user_id       bigint      not null,

    constraint pk_spending_patterns primary key (id),
    constraint fk_spending_patterns_user foreign key (user_id) references user_account (id)
);

-- Create spending_pattern_metadata table
create table spending_pattern_metadata
(
    pattern_id     bigint       not null,
    metadata_key   varchar(255) not null,
    metadata_value varchar(255),

    constraint pk_spending_pattern_metadata primary key (pattern_id, metadata_key),
    constraint fk_spending_pattern_metadata foreign key (pattern_id) references spending_patterns (id)
);
