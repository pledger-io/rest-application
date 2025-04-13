CREATE TABLE contract
(
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(255) NOT NULL,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NOT NULL,
    company_id BIGINT,
    user_id    BIGINT       NOT NULL,
    file_token VARCHAR(255),
    archived   BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_contract PRIMARY KEY (id),
    CONSTRAINT fk_contract_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_contract_account FOREIGN KEY (company_id) REFERENCES account (id)
);
