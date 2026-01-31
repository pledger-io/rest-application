CREATE TABLE rule
(
    id          BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    name        VARCHAR(255) NOT NULL,
    restrictive BOOLEAN      NOT NULL DEFAULT true,
    active      BOOLEAN      NOT NULL DEFAULT true,
    user_id     BIGINT       NOT NULL,

    CONSTRAINT pk_rule PRIMARY KEY (id),
    CONSTRAINT fk_rule_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE rule_condition
(
    id         BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    field      INTEGER      NOT NULL,
    operation  INTEGER      NOT NULL,
    cond_value VARCHAR(255) NOT NULL,
    rule_id    BIGINT       NOT NULL,

    CONSTRAINT pk_rule_condition PRIMARY KEY (id),
    CONSTRAINT fk_rule_condition_rule FOREIGN KEY (rule_id) REFERENCES rule (id)
);

CREATE TABLE rule_change
(
    id      BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    field   INTEGER      NOT NULL,
    value   VARCHAR(255) NOT NULL,
    rule_id BIGINT       NOT NULL,

    CONSTRAINT pk_rule_change PRIMARY KEY (id),
    CONSTRAINT fk_rule_change_rule FOREIGN KEY (rule_id) REFERENCES rule (id)
);
