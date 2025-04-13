CREATE TABLE tags
(
    id       BIGSERIAL     NOT NULL,
    name     VARCHAR(255)  NOT NULL,
    user_id  BIGINT        NOT NULL,
    archived BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_tags PRIMARY KEY (id),
    CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE transaction_tag
(
    tag_id         BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,

    CONSTRAINT pk_transaction_tag PRIMARY KEY (tag_id, transaction_id),
    CONSTRAINT fk_transaction_tag FOREIGN KEY (tag_id) REFERENCES tags (id),
    CONSTRAINT fk_tag_transaction FOREIGN KEY (transaction_id) REFERENCES transaction_journal (id)
);
