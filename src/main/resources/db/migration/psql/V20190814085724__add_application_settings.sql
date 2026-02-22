CREATE TABLE setting
(
    id    BIGSERIAL    NOT NULL,
    name  VARCHAR(255) NOT NULL,
    type  VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,

    CONSTRAINT pk_setting PRIMARY KEY (id),
    CONSTRAINT uq_setting UNIQUE (name)
);

INSERT INTO setting (name, type, value)
VALUES ('RegistrationOpen', 'FLAG', '1'),
       ('ImportOutdated', 'FLAG', '1'),
       ('AnalysisBudgetMonths', 'NUMBER', '3');
