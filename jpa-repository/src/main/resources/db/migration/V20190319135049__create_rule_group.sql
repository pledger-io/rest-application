create table rule_group
(
  id       bigint       not null auto_increment,
  name     varchar(255) not null,
  sort     int          not null,
  user_id  bigint       not null,
  archived bit          not null default false,

  constraint pk_rule_group primary key (id),
  constraint fk_rule_group_user foreign key (user_id) references user_account (id)
);

alter table rule
  add column group_id bigint;
alter table rule
  add constraint fk_rule_rule_group foreign key (group_id) references rule_group (id);
