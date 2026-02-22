CREATE TABLE user_account_token
(
    id            BIGSERIAL           NOT NULL,
    refresh_token VARCHAR(255) UNIQUE NOT NULL,
    expires       TIMESTAMP           NOT NULL,
    user_id       BIGINT              NOT NULL,

    CONSTRAINT pk_user_account_token PRIMARY KEY (id),
    CONSTRAINT fk_token_user_account FOREIGN KEY (user_id) REFERENCES user_account (id)
);
