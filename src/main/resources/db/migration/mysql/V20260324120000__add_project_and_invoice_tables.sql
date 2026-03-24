create table client
(
    id         bigint       not null auto_increment,
    name       varchar(255) not null,
    email      varchar(255),
    phone      varchar(100),
    address    varchar(5000),
    archived   bit          not null default false,
    user_id    bigint       not null,
    constraint pk_client primary key (id),
    constraint fk_client_user foreign key (user_id) references user_account (id)
);

create table project
(
    id          bigint       not null auto_increment,
    name        varchar(255) not null,
    description varchar(5000),
    client_id   bigint       not null,
    start_date  date,
    end_date    date,
    billable    bit          not null default true,
    archived    bit          not null default false,
    user_id     bigint       not null,
    constraint pk_project primary key (id),
    constraint fk_project_client foreign key (client_id) references client (id),
    constraint fk_project_user foreign key (user_id) references user_account (id)
);

create table time_entry
(
    id          bigint         not null auto_increment,
    project_id  bigint         not null,
    date        date           not null,
    hours       decimal(19, 4) not null,
    description varchar(2000),
    invoiced    bit            not null default false,
    user_id     bigint         not null,
    constraint pk_time_entry primary key (id),
    constraint fk_time_entry_project foreign key (project_id) references project (id),
    constraint fk_time_entry_user foreign key (user_id) references user_account (id)
);

create table invoice_template
(
    id              bigint       not null auto_increment,
    name            varchar(255) not null,
    header_content  text,
    footer_content  text,
    logo_token      varchar(255),
    user_id         bigint       not null,
    constraint pk_invoice_template primary key (id),
    constraint fk_invoice_template_user foreign key (user_id) references user_account (id)
);

create table tax_bracket
(
    id       bigint         not null auto_increment,
    name     varchar(255)   not null,
    rate     decimal(19, 4) not null,
    user_id  bigint         not null,
    constraint pk_tax_bracket primary key (id),
    constraint fk_tax_bracket_user foreign key (user_id) references user_account (id)
);

create table invoice
(
    id              bigint       not null auto_increment,
    invoice_number  varchar(255) not null,
    client_id       bigint       not null,
    invoice_date    date         not null,
    due_date        date         not null,
    template_id     bigint       not null,
    finalized       bit          not null default false,
    pdf_token       varchar(500),
    user_id         bigint       not null,
    constraint pk_invoice primary key (id),
    constraint fk_invoice_client foreign key (client_id) references client (id),
    constraint fk_invoice_template foreign key (template_id) references invoice_template (id),
    constraint fk_invoice_user foreign key (user_id) references user_account (id)
);

create table invoice_line
(
    id              bigint         not null auto_increment,
    invoice_id      bigint         not null,
    description     varchar(2000)  not null,
    quantity        decimal(19, 4) not null,
    unit            varchar(100)   not null,
    unit_price      decimal(19, 4) not null,
    tax_bracket_id  bigint         not null,
    constraint pk_invoice_line primary key (id),
    constraint fk_invoice_line_invoice foreign key (invoice_id) references invoice (id),
    constraint fk_invoice_line_tax foreign key (tax_bracket_id) references tax_bracket (id)
);

create table invoice_line_time_entry
(
    invoice_line_id bigint not null,
    time_entry_id   bigint not null,
    constraint pk_invoice_line_time_entry primary key (invoice_line_id, time_entry_id),
    constraint fk_iline_te_line foreign key (invoice_line_id) references invoice_line (id),
    constraint fk_iline_te_te foreign key (time_entry_id) references time_entry (id)
);
