create table rule
(
  id          bigint       not null auto_increment,
  name        varchar(255) not null,
  restrictive bit          not null default true,
  active      bit          not null default true,
  user_id     bigint       not null,

  constraint pk_rule primary key (id),
  constraint fk_rule_user foreign key (user_id) references user_account (id)
);

create table rule_condition
(
  id         bigint       not null auto_increment,
  field      int          not null,
  operation  int          not null,
  cond_value varchar(255) not null,
  rule_id    bigint       not null,

  constraint pk_rule_condition primary key (id),
  constraint fk_rule_condition_rule foreign key (rule_id) references rule (id)
);

create table rule_change
(
  id      bigint       not null auto_increment,
  field   int          not null,
  `value` varchar(255) not null,
  rule_id bigint       not null,

  constraint pk_rule_change primary key (id),
  constraint fk_rule_change_rule foreign key (rule_id) references rule (id)
);
