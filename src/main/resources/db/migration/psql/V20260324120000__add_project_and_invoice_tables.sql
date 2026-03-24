CREATE TABLE client
(
    id         BIGSERIAL       NOT NULL,
    name       VARCHAR(255)    NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(100),
    address    VARCHAR(5000),
    archived   BOOLEAN         NOT NULL DEFAULT FALSE,
    user_id    BIGINT          NOT NULL,
    CONSTRAINT pk_client PRIMARY KEY (id),
    CONSTRAINT fk_client_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE project
(
    id          BIGSERIAL       NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(5000),
    client_id   BIGINT          NOT NULL,
    start_date  DATE,
    end_date    DATE,
    billable    BOOLEAN         NOT NULL DEFAULT TRUE,
    archived    BOOLEAN         NOT NULL DEFAULT FALSE,
    user_id     BIGINT          NOT NULL,
    CONSTRAINT pk_project PRIMARY KEY (id),
    CONSTRAINT fk_project_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE time_entry
(
    id          BIGSERIAL       NOT NULL,
    project_id  BIGINT          NOT NULL,
    date        DATE            NOT NULL,
    hours       DECIMAL(19, 4)  NOT NULL,
    description VARCHAR(2000),
    invoiced    BOOLEAN         NOT NULL DEFAULT FALSE,
    user_id     BIGINT          NOT NULL,
    CONSTRAINT pk_time_entry PRIMARY KEY (id),
    CONSTRAINT fk_time_entry_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_time_entry_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE invoice_template
(
    id              BIGSERIAL       NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    header_content  TEXT,
    footer_content  TEXT,
    logo_token      VARCHAR(255),
    user_id         BIGINT          NOT NULL,
    CONSTRAINT pk_invoice_template PRIMARY KEY (id),
    CONSTRAINT fk_invoice_template_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE tax_bracket
(
    id       BIGSERIAL       NOT NULL,
    name     VARCHAR(255)    NOT NULL,
    rate     DECIMAL(19, 4)  NOT NULL,
    user_id  BIGINT          NOT NULL,
    CONSTRAINT pk_tax_bracket PRIMARY KEY (id),
    CONSTRAINT fk_tax_bracket_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE invoice
(
    id              BIGSERIAL       NOT NULL,
    invoice_number  VARCHAR(255)    NOT NULL,
    client_id       BIGINT          NOT NULL,
    invoice_date    DATE            NOT NULL,
    due_date        DATE            NOT NULL,
    template_id     BIGINT          NOT NULL,
    finalized       BOOLEAN         NOT NULL DEFAULT FALSE,
    pdf_token       VARCHAR(500),
    user_id         BIGINT          NOT NULL,
    CONSTRAINT pk_invoice PRIMARY KEY (id),
    CONSTRAINT fk_invoice_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT fk_invoice_template FOREIGN KEY (template_id) REFERENCES invoice_template (id),
    CONSTRAINT fk_invoice_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE invoice_line
(
    id               BIGSERIAL       NOT NULL,
    invoice_id       BIGINT          NOT NULL,
    description      VARCHAR(2000)   NOT NULL,
    quantity         DECIMAL(19, 4)  NOT NULL,
    unit             VARCHAR(100)    NOT NULL,
    unit_price       DECIMAL(19, 4)  NOT NULL,
    tax_bracket_id   BIGINT          NOT NULL,
    CONSTRAINT pk_invoice_line PRIMARY KEY (id),
    CONSTRAINT fk_invoice_line_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id),
    CONSTRAINT fk_invoice_line_tax FOREIGN KEY (tax_bracket_id) REFERENCES tax_bracket (id)
);

CREATE TABLE invoice_line_time_entry
(
    invoice_line_id BIGINT NOT NULL,
    time_entry_id   BIGINT NOT NULL,
    CONSTRAINT pk_invoice_line_time_entry PRIMARY KEY (invoice_line_id, time_entry_id),
    CONSTRAINT fk_iline_te_line FOREIGN KEY (invoice_line_id) REFERENCES invoice_line (id),
    CONSTRAINT fk_iline_te_te FOREIGN KEY (time_entry_id) REFERENCES time_entry (id)
);
