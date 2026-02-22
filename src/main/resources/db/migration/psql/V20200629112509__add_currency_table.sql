CREATE TABLE currency
(
    id             BIGSERIAL     NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    symbol         CHAR(1)       NOT NULL,
    code           CHAR(3)       NOT NULL,
    decimal_places INT           NOT NULL DEFAULT 2,

    enabled        BOOLEAN       NOT NULL DEFAULT TRUE,
    archived       BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_currency PRIMARY KEY (id),
    CONSTRAINT uk_currency_code UNIQUE (code)
);

INSERT INTO currency (name, symbol, code, decimal_places, enabled, archived)
VALUES ('Euro', '€', 'EUR', DEFAULT, DEFAULT, DEFAULT),
       ('US Dollar', '$', 'USD', DEFAULT, DEFAULT, DEFAULT),
       ('British Pound', '£', 'GBP', DEFAULT, DEFAULT, DEFAULT);
