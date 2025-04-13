CREATE TABLE account_synonym
(
    id         BIGSERIAL    NOT NULL,
    account_id BIGINT       NOT NULL,
    synonym    VARCHAR(255) NOT NULL,

    CONSTRAINT pk_account_synonym PRIMARY KEY (id),
    CONSTRAINT fk_account_synonym_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT uq_synonym UNIQUE (synonym)
);
