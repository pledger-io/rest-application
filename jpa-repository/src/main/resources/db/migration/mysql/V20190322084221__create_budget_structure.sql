create table budget
(
  id              bigint          not null auto_increment,
  expected_income decimal(22, 12) not null,
  b_from          timestamp       not null,
  b_until         timestamp,
  user_id         bigint          not null,

  constraint pk_budget primary key (id),
  constraint fk_budget_user foreign key (user_id) references user_account (id),
  constraint ck_budget_frame check (b_from < b_until)
);

create table budget_expense
(
  id       bigint       not null auto_increment,
  name     varchar(255) not null,
  user_id  bigint       not null,
  archived bit          not null default false,

  constraint pk_budget_expense primary key (id),
  constraint fk_budget_expense_user foreign key (user_id) references user_account (id)
);

create table budget_period
(
  id             bigint          not null auto_increment,
  bp_lower_bound decimal(22, 2) not null,
  bp_upper_bound decimal(22, 2) not null,

  budget_id      bigint          not null,
  expense_id     bigint          not null,

  constraint pk_budget_period primary key (id),
  constraint fk_budget_period_budget_expense foreign key (expense_id) references budget_expense (id),
  constraint ck_budget_period_bounds check (bp_lower_bound < bp_upper_bound),
  constraint fk_budget_expense_budget foreign key (budget_id) references budget (id)
);
