CREATE TABLE transaction_schedule
(
    id             BIGSERIAL      NOT NULL,

    name           VARCHAR(255),
    description    VARCHAR(1024),
    amount         DECIMAL(22, 2) NOT NULL,

    periodicity    CHAR(10),
    reoccur        INT,
    start_date     DATE,
    end_date       DATE,

    user_id        BIGINT        NOT NULL,
    source_id      BIGINT        NOT NULL,
    destination_id BIGINT        NOT NULL,

    CONSTRAINT pk_transaction_schedule PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_source_account FOREIGN KEY (source_id) REFERENCES account (id),
    CONSTRAINT fk_destination_account FOREIGN KEY (destination_id) REFERENCES account (id)
);
