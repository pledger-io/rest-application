CREATE TABLE budget
(
    id              BIGSERIAL       NOT NULL,
    expected_income DECIMAL(22, 12) NOT NULL,
    b_from          TIMESTAMP       NOT NULL,
    b_until         TIMESTAMP,
    user_id         BIGINT          NOT NULL,

    CONSTRAINT pk_budget PRIMARY KEY (id),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT ck_budget_frame CHECK (b_from < b_until)
);

CREATE TABLE budget_expense
(
    id       BIGSERIAL     NOT NULL,
    name     VARCHAR(255)  NOT NULL,
    user_id  BIGINT        NOT NULL,
    archived BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_budget_expense PRIMARY KEY (id),
    CONSTRAINT fk_budget_expense_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE budget_period
(
    id             BIGSERIAL      NOT NULL,
    bp_lower_bound DECIMAL(22, 2) NOT NULL,
    bp_upper_bound DECIMAL(22, 2) NOT NULL,
    budget_id      BIGINT         NOT NULL,
    expense_id     BIGINT         NOT NULL,

    CONSTRAINT pk_budget_period PRIMARY KEY (id),
    CONSTRAINT fk_budget_period_budget_expense FOREIGN KEY (expense_id) REFERENCES budget_expense (id),
    CONSTRAINT ck_budget_period_bounds CHECK (bp_lower_bound < bp_upper_bound),
    CONSTRAINT fk_budget_expense_budget FOREIGN KEY (budget_id) REFERENCES budget (id)
);
